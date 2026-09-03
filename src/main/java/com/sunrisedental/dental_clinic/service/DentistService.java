package com.sunrisedental.dental_clinic.service;

import com.sunrisedental.dental_clinic.dto.DentistRequest;
import com.sunrisedental.dental_clinic.dto.DentistResponse;
import com.sunrisedental.dental_clinic.exception.ResourceNotFoundException;
import com.sunrisedental.dental_clinic.model.Appointment;
import com.sunrisedental.dental_clinic.model.Dentist;
import com.sunrisedental.dental_clinic.repository.AppointmentRepository;
import com.sunrisedental.dental_clinic.repository.DentistRepository;
import com.sunrisedental.dental_clinic.repository.InvoiceRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DentistService {

    private final DentistRepository dentistRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    public DentistService(DentistRepository dentistRepository,
                          AppointmentRepository appointmentRepository,
                          InvoiceRepository invoiceRepository) {
        this.dentistRepository = dentistRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
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

    public void deleteDentist(Long id) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));
        List<Appointment> appointments = appointmentRepository.findByDentist(dentist);
        for (Appointment apt : appointments) {
            invoiceRepository.findByAppointment(apt).ifPresent(invoiceRepository::delete);
            appointmentRepository.delete(apt);
        }
        dentistRepository.delete(dentist);
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
