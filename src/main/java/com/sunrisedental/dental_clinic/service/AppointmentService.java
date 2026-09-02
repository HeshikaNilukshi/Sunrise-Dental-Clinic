package com.sunrisedental.dental_clinic.service;

import com.sunrisedental.dental_clinic.dto.AppointmentRequest;
import com.sunrisedental.dental_clinic.dto.AppointmentResponse;
import com.sunrisedental.dental_clinic.exception.DuplicateBookingException;
import com.sunrisedental.dental_clinic.exception.ResourceNotFoundException;
import com.sunrisedental.dental_clinic.model.Appointment;
import com.sunrisedental.dental_clinic.model.Dentist;
import com.sunrisedental.dental_clinic.model.Patient;
import com.sunrisedental.dental_clinic.model.enums.AppointmentStatus;
import com.sunrisedental.dental_clinic.repository.AppointmentRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DentistService dentistService;
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientService patientService,
                              DentistService dentistService,
                              EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.dentistService = dentistService;
        this.emailService = emailService;
    }

    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        Patient patient = patientService.getPatientEntityById(request.getPatientId());
        Dentist dentist = dentistService.getDentistEntityById(request.getDentistId());

        if (!dentist.isActive()) {
            throw new IllegalStateException("Selected dentist is currently inactive");
        }

        boolean hasConflict = appointmentRepository.existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusNot(
                dentist,
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                AppointmentStatus.CANCELLED
        );

        if (hasConflict) {
            throw new DuplicateBookingException("Dentist " + dentist.getFullName()
                    + " already has an active appointment on " + request.getAppointmentDate()
                    + " at " + request.getAppointmentTime());
        }

        String appointmentNumber = generateAppointmentNumber(request.getAppointmentDate());

        Appointment appointment = new Appointment(
                appointmentNumber,
                patient,
                dentist,
                request.getTreatmentType(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                request.getNotes()
        );

        Appointment saved = appointmentRepository.save(appointment);
        AppointmentResponse response = mapToResponse(saved);
        emailService.sendBookingConfirmation(response);
        return response;
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getByAppointmentNumber(String appointmentNumber) {
        Appointment appointment = appointmentRepository.findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with number: " + appointmentNumber));
        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentEntityByNumber(String appointmentNumber) {
        return appointmentRepository.findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with number: " + appointmentNumber));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientHistory(Long patientId) {
        // Verify patient exists
        patientService.getPatientEntityById(patientId);
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse cancelAppointment(String appointmentNumber) {
        Appointment appointment = getAppointmentEntityByNumber(appointmentNumber);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updated = appointmentRepository.save(appointment);
        AppointmentResponse response = mapToResponse(updated);
        emailService.sendCancellationNotice(response);
        return response;
    }

    public AppointmentResponse completeAppointment(String appointmentNumber) {
        Appointment appointment = getAppointmentEntityByNumber(appointmentNumber);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    private String generateAppointmentNumber(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long dailyCount = appointmentRepository.countByAppointmentDate(date);
        long sequence = dailyCount + 1;
        String candidateNumber = "APT-" + dateStr + "-" + String.format("%03d", sequence);

        while (appointmentRepository.existsByAppointmentNumber(candidateNumber)) {
            sequence++;
            candidateNumber = "APT-" + dateStr + "-" + String.format("%03d", sequence);
        }
        return candidateNumber;
    }

    public AppointmentResponse mapToResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getAppointmentNumber(),
                a.getPatient().getId(),
                a.getPatient().getFullName(),
                a.getPatient().getContactNumber(),
                a.getPatient().getEmail(),
                a.getPatient().getAddress(),
                a.getDentist().getId(),
                a.getDentist().getFullName(),
                a.getDentist().getSpecialization(),
                a.getTreatmentType(),
                a.getTreatmentType().getBaseCost(),
                a.getTreatmentType().getConsultationFee(),
                a.getTreatmentType().calculateTotalCost(),
                a.getAppointmentDate(),
                a.getAppointmentTime(),
                a.getNotes(),
                a.getStatus(),
                a.getCreatedAt()
        );
    }
}
