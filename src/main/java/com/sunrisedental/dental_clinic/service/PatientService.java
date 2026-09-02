package com.sunrisedental.dental_clinic.service;

import com.sunrisedental.dental_clinic.dto.PatientRequest;
import com.sunrisedental.dental_clinic.dto.PatientResponse;
import com.sunrisedental.dental_clinic.exception.DuplicateResourceException;
import com.sunrisedental.dental_clinic.exception.ResourceNotFoundException;
import com.sunrisedental.dental_clinic.model.Patient;
import com.sunrisedental.dental_clinic.repository.PatientRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientResponse registerPatient(PatientRequest request) {
        if (patientRepository.existsByContactNumber(request.getContactNumber())) {
            throw new DuplicateResourceException("Patient with contact number " + request.getContactNumber() + " already exists");
        }

        Patient patient = new Patient(
                request.getFullName(),
                request.getAddress(),
                request.getContactNumber(),
                request.getEmail(),
                request.getDateOfBirth()
        );

        Patient saved = patientRepository.save(patient);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return mapToResponse(patient);
    }

    @Transactional(readOnly = true)
    public Patient getPatientEntityById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> searchByName(String name) {
        return patientRepository.findByFullNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientResponse findByContact(String contactNumber) {
        Patient patient = patientRepository.findByContactNumber(contactNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with contact number: " + contactNumber));
        return mapToResponse(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PatientResponse mapToResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getAddress(),
                patient.getContactNumber(),
                patient.getEmail(),
                patient.getDateOfBirth(),
                patient.getRegistrationDate()
        );
    }
}
