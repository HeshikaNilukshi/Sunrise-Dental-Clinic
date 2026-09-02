package com.sunrisedental.dental_clinic.service;

import com.sunrisedental.dental_clinic.dto.DentistRequest;
import com.sunrisedental.dental_clinic.dto.DentistResponse;
import com.sunrisedental.dental_clinic.exception.ResourceNotFoundException;
import com.sunrisedental.dental_clinic.model.Dentist;
import com.sunrisedental.dental_clinic.repository.DentistRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DentistService {

    private final DentistRepository dentistRepository;

    public DentistService(DentistRepository dentistRepository) {
        this.dentistRepository = dentistRepository;
    }

    public DentistResponse createDentist(DentistRequest request) {
        Dentist dentist = new Dentist(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getSpecialization(),
                request.isActive()
        );
        Dentist saved = dentistRepository.save(dentist);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DentistResponse> getActiveDentists() {
        return dentistRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DentistResponse getDentistById(Long id) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));
        return mapToResponse(dentist);
    }

    @Transactional(readOnly = true)
    public Dentist getDentistEntityById(Long id) {
        return dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<DentistResponse> getAllDentists() {
        return dentistRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DentistResponse mapToResponse(Dentist dentist) {
        return new DentistResponse(
                dentist.getId(),
                dentist.getFullName(),
                dentist.getEmail(),
                dentist.getPhone(),
                dentist.getSpecialization(),
                dentist.isActive()
        );
    }
}
