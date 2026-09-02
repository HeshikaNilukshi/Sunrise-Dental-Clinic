package com.sunrisedental.dental_clinic.controller;

import com.sunrisedental.dental_clinic.dto.DentistRequest;
import com.sunrisedental.dental_clinic.dto.DentistResponse;
import com.sunrisedental.dental_clinic.service.DentistService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dentists")
public class DentistController {

    private final DentistService dentistService;

    public DentistController(DentistService dentistService) {
        this.dentistService = dentistService;
    }

    @PostMapping
    public ResponseEntity<DentistResponse> createDentist(@Valid @RequestBody DentistRequest request) {
        DentistResponse response = dentistService.createDentist(request);
        return ResponseEntity.created(URI.create("/api/dentists/" + response.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DentistResponse>> getActiveDentists() {
        return ResponseEntity.ok(dentistService.getActiveDentists());
    }

    @GetMapping("/all")
    public ResponseEntity<List<DentistResponse>> getAllDentists() {
        return ResponseEntity.ok(dentistService.getAllDentists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DentistResponse> getDentistById(@PathVariable Long id) {
        return ResponseEntity.ok(dentistService.getDentistById(id));
    }
}
