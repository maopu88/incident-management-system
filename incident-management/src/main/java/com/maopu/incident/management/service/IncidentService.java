package com.maopu.incident.management.service;

import com.maopu.incident.management.entity.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface IncidentService {

    /**
     * 分页列表查询
     *
     * @param title
     * @param pageable
     * @return
     */
    Page<Incident> getPage(String title, Pageable pageable);

    /**
     * 查询详情
     *
     * @param id
     * @return
     */
    Incident findById(Long id);

    /**
     * 创建事件
     *
     * @param incident
     * @return
     */
    Incident createIncident(Incident incident);

    /**
     * 修改事件
     *
     * @param id
     * @param incidentDetails
     * @return
     */
    Incident updateIncident(Long id, Incident incidentDetails);

    /**
     * 删除事件
     *
     * @param id
     */
    void deleteIncident(Long id);
}