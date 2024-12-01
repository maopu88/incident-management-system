package com.maopu.incident.management.controller;


import com.maopu.incident.management.entity.Incident;
import com.maopu.incident.management.response.Response;
import com.maopu.incident.management.response.ResponseFactory;
import com.maopu.incident.management.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @GetMapping
    public ResponseEntity<Response<Page<Incident>>> getAllIncidents(@RequestParam(value = "title", required = false) String title,
                                                                    @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                                    @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok(ResponseFactory.getSuccessData(incidentService.getPage(title, PageRequest.of(page, size,Sort.by("createdAt").descending()))));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Response> getIncidentInfo(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(ResponseFactory.getSuccessData(incidentService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<Response> createIncident(@Valid @RequestBody Incident incident) {
        return ResponseEntity.ok(ResponseFactory.getSuccessData(incidentService.createIncident(incident)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateIncident(@PathVariable(value = "id") Long id, @Valid @RequestBody Incident incident) {
        return ResponseEntity.ok(ResponseFactory.getSuccessData(incidentService.updateIncident(id, incident)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteIncident(@PathVariable(value = "id") Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.ok(ResponseFactory.getSuccess());
    }
}
