package com.maopu.incident.management.repository;

import com.maopu.incident.management.entity.Incident;
import org.hibernate.annotations.NamedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    /**
     * 模糊查询标题分页
     *
     * @param title
     * @param pageable
     * @return
     */
    Page<Incident> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 是否存在该名称标题
     *
     * @param title
     * @return
     */
    boolean existsByTitle(String title);

    /**
     * 查询除了当前事件标题外是否存在该名称标题事件
     *
     * @param title
     * @param id
     * @return
     */
    boolean existsByTitleAndIdNot(String title, long id);

}
