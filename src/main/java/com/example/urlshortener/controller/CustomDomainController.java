package com.example.urlshortener.controller;

import com.example.urlshortener.model.CustomDomain;
import com.example.urlshortener.service.CustomDomainService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/domains")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CustomDomainController {

    private final CustomDomainService service;

    @Data
    static class CreateDto { private String domain; }

    @PostMapping
    public ResponseEntity<CustomDomain> create(@RequestBody CreateDto dto) {
        CustomDomain cd = service.addDomain(dto.getDomain());
        // return 201 with Location
        return ResponseEntity.created(URI.create("/api/domains/" + cd.getId())).body(cd);
    }

    @GetMapping
    public ResponseEntity<List<CustomDomain>> list() {
        return ResponseEntity.ok(service.listDomains());
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyNow(@PathVariable Long id) {
        boolean ok = service.verifyDomainNow(id);
        if (ok) return ResponseEntity.ok("verified");
        return ResponseEntity.status(409).body("not verified");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteDomain(id);
        return ResponseEntity.noContent().build();
    }
}