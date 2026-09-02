package com.sunrisedental.dental_clinic.dto;

import com.sunrisedental.dental_clinic.model.enums.AppointmentStatus;
import com.sunrisedental.dental_clinic.model.enums.TreatmentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentResponse {

    private Long id;
    private String appointmentNumber;
    private Long patientId;
    private String patientName;
    private String patientContact;
    private String patientEmail;
    private String patientAddress;
    private Long dentistId;
    private String dentistName;
    private String dentistSpecialization;
    private TreatmentType treatmentType;
    private BigDecimal treatmentCost;
    private BigDecimal consultationFee;
    private BigDecimal totalCost;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String notes;
    private AppointmentStatus status;
    private LocalDateTime createdAt;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long id, String appointmentNumber, Long patientId, String patientName,
                               String patientContact, String patientEmail, String patientAddress,
                               Long dentistId, String dentistName, String dentistSpecialization,
                               TreatmentType treatmentType, BigDecimal treatmentCost,
                               BigDecimal consultationFee, BigDecimal totalCost,
                               LocalDate appointmentDate, LocalTime appointmentTime,
                               String notes, AppointmentStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.appointmentNumber = appointmentNumber;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientContact = patientContact;
        this.patientEmail = patientEmail;
        this.patientAddress = patientAddress;
        this.dentistId = dentistId;
        this.dentistName = dentistName;
        this.dentistSpecialization = dentistSpecialization;
        this.treatmentType = treatmentType;
        this.treatmentCost = treatmentCost;
        this.consultationFee = consultationFee;
        this.totalCost = totalCost;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientContact() {
        return patientContact;
    }

    public void setPatientContact(String patientContact) {
        this.patientContact = patientContact;
    }

    public String getPatientEmail() {
        return email();
    }

    public String email() {
        return patientEmail;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public Long getDentistId() {
        return dentistId;
    }

    public void setDentistId(Long dentistId) {
        this.dentistId = dentistId;
    }

    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(String dentistName) {
        this.dentistName = dentistName;
    }

    public String getDentistSpecialization() {
        return dentistSpecialization;
    }

    public void setDentistSpecialization(String dentistSpecialization) {
        this.dentistSpecialization = dentistSpecialization;
    }

    public TreatmentType getTreatmentType() {
        return treatmentType;
    }

    public void setTreatmentType(TreatmentType treatmentType) {
        this.treatmentType = treatmentType;
    }

    public BigDecimal getTreatmentCost() {
        return treatmentCost;
    }

    public void setTreatmentCost(BigDecimal treatmentCost) {
        this.treatmentCost = treatmentCost;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
