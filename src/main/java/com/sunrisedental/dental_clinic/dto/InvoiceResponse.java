package com.sunrisedental.dental_clinic.dto;

import com.sunrisedental.dental_clinic.model.enums.PaymentStatus;
import com.sunrisedental.dental_clinic.model.enums.TreatmentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private String appointmentNumber;
    private Long patientId;
    private String patientName;
    private String dentistName;
    private TreatmentType treatmentType;
    private BigDecimal treatmentCost;
    private BigDecimal consultationFee;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime generatedDate;

    public InvoiceResponse() {
    }

    public InvoiceResponse(Long id, String invoiceNumber, String appointmentNumber, Long patientId,
                           String patientName, String dentistName, TreatmentType treatmentType,
                           BigDecimal treatmentCost, BigDecimal consultationFee, BigDecimal totalAmount,
                           PaymentStatus paymentStatus, LocalDateTime generatedDate) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.appointmentNumber = appointmentNumber;
        this.patientId = patientId;
        this.patientName = patientName;
        this.dentistName = dentistName;
        this.treatmentType = treatmentType;
        this.treatmentCost = treatmentCost;
        this.consultationFee = consultationFee;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.generatedDate = generatedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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

    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(String dentistName) {
        this.dentistName = dentistName;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }
}
