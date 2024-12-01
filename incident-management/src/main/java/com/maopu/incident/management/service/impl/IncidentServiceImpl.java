package com.maopu.incident.management.service.impl;


import com.google.common.hash.BloomFilter;
import com.maopu.incident.management.entity.Incident;
import com.maopu.incident.management.exception.ServiceException;
import com.maopu.incident.management.repository.IncidentRepository;
import com.maopu.incident.management.response.ResponseEnum;
import com.maopu.incident.management.service.IncidentService;
import com.maopu.incident.management.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class IncidentServiceImpl implements IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private BloomFilter<String> bloomFilter;

    @Autowired
    private CacheManager cacheManager;
    /**
     * 使用 ConcurrentHashMap 来存储每个 ID 对应的锁
     */
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();


    @Override
    @Cacheable(value = Constants.CACHE_NAME_INCIDENTS, key = "#pageable.pageNumber + '_' + #title + '_' + #pageable.pageSize", unless = "#result == null")
    public Page<Incident> getPage(String title, Pageable pageable) {
        if (StringUtils.isBlank(title)) {
            return incidentRepository.findAll(pageable);
        }
        return incidentRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    @Override
    public Incident findById(Long id) {
        String key = id.toString();
        if (!bloomFilter.mightContain(key)) {
            // 如果布隆过滤器认为该键不存在，则直接返回空结果
            return null;
        }
        // 首先检查缓存
        if (cacheContains(id)) {
            return getFromCache(id);
        }
        ReentrantLock lock = locks.computeIfAbsent(id, k -> new ReentrantLock());
        // 定义重试次数
        int retryCount = 3;
        while (retryCount > 0) {
            try {
                // 尝试获取锁，设置超时时间为2秒
                if (lock.tryLock(2, TimeUnit.SECONDS)) {
                    try {
                        // 双重检查：确保在获得锁后缓存数据未被其他线程填入
                        if (cacheContains(id)) {
                            return getFromCache(id);
                        }
                        // 缓存仍然不存在，查询数据库
                        Incident incident = incidentRepository.findById(id).orElse(null);
                        // 将数据库结果放入缓存（即使为空）
                        putInCache(id, incident);
                        return incident;
                    } finally {
                        // 确保锁被释放
                        lock.unlock();
                    }
                } else {
                    // 减少重试次数
                    retryCount--;
                    log.warn("Could not acquire lock for id: {}. Retrying... ({} retries left)", id, retryCount);
                    try {
                        // 在稍长的间隔前等待，避免过度的 CPU 使用
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted while waiting to retry lock acquisition for id: {}, error:{}", id, e.getMessage(), e);
                    }
                }
            } catch (InterruptedException e) {
                // 重新设置中断状态
                Thread.currentThread().interrupt();
                log.error("Interrupted while trying to acquire lock for id: {}, error:{}", id, e.getMessage(), e);
                throw new ServiceException(ResponseEnum.failure.getCode());
            }
        }
        // 在所有重试之后仍不能获得锁，可能返回一个标识值或抛出异常
        log.error("Failed to acquire lock for id: {} after multiple attempts", id);
        // 或者考虑抛出自定义异常以反映重试失败
        throw new ServiceException(ResponseEnum.failure.getCode());
    }

    /**
     * 检查缓存是否包含指定 ID 的数据
     *
     * @param id
     * @return
     */
    private boolean cacheContains(Long id) {
        Cache cache = cacheManager.getCache(Constants.CACHE_NAME_INCIDENT);
        if (cache == null) {
            // 记录日志，通知开发者缓存不存在
            log.warn("Cache '{}' is not available in the cache manager.", Constants.CACHE_NAME_INCIDENT);
            return false;
        }
        // 使用 Optional 来处理可能的空值
        Optional<Incident> optionalValue = Optional.ofNullable(cache.get(id, Incident.class));
        return optionalValue.isPresent();
    }

    /**
     * 从缓存中获取数据
     *
     * @param id
     * @return
     */
    private Incident getFromCache(Long id) {
        Cache cache = cacheManager.getCache(Constants.CACHE_NAME_INCIDENT);
        Incident incident = cache.get(id, Incident.class);
        return incident;
    }

    /**
     * 将数据放入缓存
     *
     * @param id
     * @param incident
     */
    private void putInCache(Long id, Incident incident) {
        Cache cache = cacheManager.getCache(Constants.CACHE_NAME_INCIDENT);
        cache.put(id, incident);
    }


    /**
     * 创建事件、更新缓存、更新布隆过滤器
     * 清除 incidents 分页缓存中的所有条目,，从而避免数据不一致的问题
     *
     * @param incident
     * @return
     */
    @Override
    @CachePut(value = Constants.CACHE_NAME_INCIDENT, key = "#result.id")
    @CacheEvict(value = Constants.CACHE_NAME_INCIDENTS, allEntries = true)
    public Incident createIncident(Incident incident) {
        if (incidentRepository.existsByTitle(incident.getTitle())) {
            throw new ServiceException("Title already exists: " + incident.getTitle());
        }
        Incident savedIncident = incidentRepository.save(incident);
        // 更新布隆过滤器
        bloomFilter.put(savedIncident.getId().toString());
        return savedIncident;
    }

    /**
     * 修改事件、只有当数据发生变化时才更新数据和更新缓存，不更新布隆过滤器因为修改id不变
     * 清除 incidents 分页缓存中的所有条目,从而避免数据不一致的问题
     *
     * @param id
     * @param incidentDetails
     * @return
     */
    @Override
    @CachePut(value = Constants.CACHE_NAME_INCIDENT, key = "#id")
    @CacheEvict(value = Constants.CACHE_NAME_INCIDENTS, allEntries = true)
    public Incident updateIncident(Long id, Incident incidentDetails) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Incident not found"));
        // 只有当数据发生变化时才更新
        if (!incident.equals(incidentDetails)) {
            boolean existsByTitleAndIdNot = incidentRepository.existsByTitleAndIdNot(incidentDetails.getTitle(), id);
            if (existsByTitleAndIdNot) {
                throw new ServiceException("Title already exists: " + incidentDetails.getTitle());
            }
            incident.setTitle(incidentDetails.getTitle());
            incident.setDescription(incidentDetails.getDescription());
            incident.setStatus(incidentDetails.getStatus());

            Incident save = incidentRepository.save(incident);
            //延迟双删策略,在更新数据库后，先删除缓存，然后在短暂延迟后再次删除缓存,延迟主要为了最大化地捕获期间的脏读取。
            cacheManager.getCache(Constants.CACHE_NAME_INCIDENT).evict(id);
            try {
                // 100ms可调
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting del cache id: {}, error:{}", id, e.getMessage(), e);
            }
            cacheManager.getCache(Constants.CACHE_NAME_INCIDENT).evict(id);
            return save;
        }
        // 如果没有变化，直接返回原来的对象,有变化返回更新后的对象
        return incident;
    }

    /**
     * 删除事件、删除缓存
     * 清除 incidents 分页缓存中的所有条目,，从而避免数据不一致的问题
     *
     * @param id
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = Constants.CACHE_NAME_INCIDENT, key = "#id"),
            @CacheEvict(value = Constants.CACHE_NAME_INCIDENTS, allEntries = true)
    })
    public void deleteIncident(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new ServiceException("Incident not found");
        }
        incidentRepository.deleteById(id);
        // 布隆过滤器不支持删除操作，在删除缓存项时，可以选择忽略这个操作
    }


}

