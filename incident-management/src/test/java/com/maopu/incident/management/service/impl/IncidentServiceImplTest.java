package com.maopu.incident.management.service.impl;

import com.google.common.hash.BloomFilter;
import com.maopu.incident.management.entity.Incident;
import com.maopu.incident.management.exception.ServiceException;
import com.maopu.incident.management.repository.IncidentRepository;
import com.maopu.incident.management.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
public class IncidentServiceImplTest {

    @InjectMocks
    private IncidentServiceImpl incidentService;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private BloomFilter<String> bloomFilter;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(cacheManager.getCache(Constants.CACHE_NAME_INCIDENT)).thenReturn(cache);
    }

    @Test
    public void testGetPage() {
        Pageable pageable = PageRequest.of(0, 1);
        Incident incident = new Incident(1L, "title", "description", Incident.IncidentStatus.OPEN, LocalDateTime.now(), LocalDateTime.now());
        Page<Incident> page = new PageImpl<>(Collections.singletonList(incident));
        when(incidentRepository.findAll(pageable)).thenReturn(page);

        Page<Incident> result = incidentService.getPage("", pageable);
        assertEquals(1, result.getTotalElements());
        verify(incidentRepository).findAll(pageable);
    }

    @Test
    public void testGetPageByTitle() {
        Pageable pageable = PageRequest.of(0, 1);
        Incident incident = new Incident(1L, "title", "description", Incident.IncidentStatus.OPEN, LocalDateTime.now(), LocalDateTime.now());
        Page<Incident> page = new PageImpl<>(Collections.singletonList(incident));
        when(incidentRepository.findByTitleContainingIgnoreCase("title", pageable)).thenReturn(page);

        Page<Incident> result = incidentService.getPage("title", pageable);
        assertEquals(1, result.getTotalElements());
        verify(incidentRepository).findByTitleContainingIgnoreCase("title", pageable);
    }

    @Test
    public void testFindById_NotInBloomFilter() {
        Long id = 1L;
        when(bloomFilter.mightContain(id.toString())).thenReturn(false);
        Incident byId = incidentService.findById(id);
        assertEquals(null, byId);
    }

    @Test
    public void testFindByIdNotCache() {
        Long id = 1L;
        when(bloomFilter.mightContain(id.toString())).thenReturn(true);
        Incident incident = new Incident(id, "title", "description", Incident.IncidentStatus.OPEN, LocalDateTime.now(), LocalDateTime.now());
        when(incidentRepository.findById(id)).thenReturn(Optional.ofNullable(incident));
        Incident result = incidentService.findById(id);
        assertNotNull(result);
        assertEquals(incident, result);
    }


    @Test
    public void testFindByIdHaveCache() {
        Long id = 1L;
        when(bloomFilter.mightContain(id.toString())).thenReturn(true);
        Incident incident = new Incident(id, "title", "description", Incident.IncidentStatus.OPEN, LocalDateTime.now(), LocalDateTime.now());
        when(cache.get(id, Incident.class)).thenReturn(incident);
        Incident result = incidentService.findById(id);
        assertNotNull(result);
        assertEquals(incident, result);
    }

    @Test
    public void testCreateIncident() {
        Incident incident = new Incident(null, "title", "description", Incident.IncidentStatus.OPEN, LocalDateTime.now(), LocalDateTime.now());
        Incident savedIncident = new Incident(1L, "title", "description", Incident.IncidentStatus.OPEN, LocalDateTime.now(), LocalDateTime.now());

        when(incidentRepository.existsByTitle(incident.getTitle())).thenReturn(false);
        when(incidentRepository.save(incident)).thenReturn(savedIncident);

        Incident result = incidentService.createIncident(incident);
        assertEquals(savedIncident, result);
        verify(incidentRepository).save(incident);
        verify(bloomFilter).put(savedIncident.getId().toString());
    }

    @Test(expected = ServiceException.class)
    public void testCreateIncident_TitleExists() {
        Incident incident = new Incident(null, "title", "description", Incident.IncidentStatus.OPEN, LocalDateTime.now(), LocalDateTime.now());
        when(incidentRepository.existsByTitle(incident.getTitle())).thenReturn(true);

        incidentService.createIncident(incident);
    }

    @Test
    public void testUpdateIncident() {
        Long id = 1L;
        Incident incidentDetails = new Incident(id, "new title", "new description", Incident.IncidentStatus.CLOSED, LocalDateTime.now(), LocalDateTime.now());
        Incident existingIncident = new Incident(id, "title", "description", Incident.IncidentStatus.OPEN, LocalDateTime.now(), LocalDateTime.now());

        when(incidentRepository.findById(id)).thenReturn(Optional.of(existingIncident));
        when(incidentRepository.existsByTitleAndIdNot(incidentDetails.getTitle(), id)).thenReturn(false);
        when(incidentRepository.save(incidentDetails)).thenReturn(incidentDetails);
        Incident updatedIncident = incidentService.updateIncident(id, incidentDetails);

        assertNotNull(updatedIncident);
        assertEquals("new title", existingIncident.getTitle());
        assertEquals("new description", existingIncident.getDescription());
        assertEquals(Incident.IncidentStatus.CLOSED, existingIncident.getStatus());
        verify(incidentRepository).save(existingIncident);
    }

    @Test(expected = ServiceException.class)
    public void testUpdateIncident_NotFound() {
        Long id = 1L;
        Incident incidentDetails = new Incident(id, "new title", "new description", Incident.IncidentStatus.CLOSED, LocalDateTime.now(), LocalDateTime.now());

        when(incidentRepository.findById(id)).thenReturn(Optional.empty());

        incidentService.updateIncident(id, incidentDetails);
    }

    @Test(expected = ServiceException.class)
    public void testUpdateIncidentExistsByTitleAndIdNot() {
        Long id = 1L;
        Incident incidentDetails = new Incident(id, "new title", "new description", Incident.IncidentStatus.CLOSED, LocalDateTime.now(), LocalDateTime.now());
        Incident existingIncident = new Incident(id, "title", "description", Incident.IncidentStatus.OPEN, LocalDateTime.now(), LocalDateTime.now());

        when(incidentRepository.findById(id)).thenReturn(Optional.of(existingIncident));
        when(incidentRepository.existsByTitleAndIdNot(incidentDetails.getTitle(), id)).thenReturn(true);

        incidentService.updateIncident(id, incidentDetails);
    }


    @Test
    public void testDeleteIncident() {
        Long id = 1L;
        when(incidentRepository.existsById(id)).thenReturn(true);

        incidentService.deleteIncident(id);

        verify(incidentRepository).deleteById(id);

    }

    @Test(expected = ServiceException.class)
    public void testDeleteIncident_NotFound() {
        Long id = 1L;
        when(incidentRepository.existsById(id)).thenReturn(false);

        incidentService.deleteIncident(id);
    }
}
