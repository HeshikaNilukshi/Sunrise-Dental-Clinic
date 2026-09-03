package com.sunrisedental.dental_clinic.controller;

import com.sunrisedental.dental_clinic.dto.AppointmentRequest;
import com.sunrisedental.dental_clinic.dto.AppointmentResponse;
import com.sunrisedental.dental_clinic.service.AppointmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(request);
        return ResponseEntity.created(URI.create("/api/appointments/" + response.getAppointmentNumber())).body(response);
    }

    @GetMapping("/{appointmentNumber}")
    public ResponseEntity<AppointmentResponse> getByAppointmentNumber(@PathVariable String appointmentNumber) {
        return ResponseEntity.ok(appointmentService.getByAppointmentNumber(appointmentNumber));
    }

    @DeleteMapping("/{appointmentNumber}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String appointmentNumber) {
        appointmentService.deleteAppointment(appointmentNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDate(date));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getPatientHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getPatientHistory(patientId));
    }

    @PatchMapping("/{appointmentNumber}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable String appointmentNumber) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentNumber));
    }

    @PatchMapping("/{appointmentNumber}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable String appointmentNumber) {
        return ResponseEntity.ok(appointmentService.completeAppointment(appointmentNumber));
    }
}
