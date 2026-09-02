package com.sunrisedental.dental_clinic.service;

import com.sunrisedental.dental_clinic.dto.InvoiceResponse;
import com.sunrisedental.dental_clinic.exception.DuplicateResourceException;
import com.sunrisedental.dental_clinic.exception.ResourceNotFoundException;
import com.sunrisedental.dental_clinic.model.Appointment;
import com.sunrisedental.dental_clinic.model.Invoice;
import com.sunrisedental.dental_clinic.model.enums.PaymentStatus;
import com.sunrisedental.dental_clinic.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final AppointmentService appointmentService;
    private final EmailService emailService;

    public BillingService(InvoiceRepository invoiceRepository,
                          AppointmentService appointmentService,
                          EmailService emailService) {
        this.invoiceRepository = invoiceRepository;
        this.appointmentService = appointmentService;
        this.emailService = emailService;
    }

    public InvoiceResponse generateInvoice(String appointmentNumber) {
        Appointment appointment = appointmentService.getAppointmentEntityByNumber(appointmentNumber);

        if (invoiceRepository.findByAppointment(appointment).isPresent()) {
            throw new DuplicateResourceException("Invoice has already been generated for appointment: " + appointmentNumber);
        }

        BigDecimal treatmentCost = appointment.getTreatmentType().getBaseCost();
        BigDecimal consultationFee = appointment.getTreatmentType().getConsultationFee();
        BigDecimal totalAmount = treatmentCost.add(consultationFee);

        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = new Invoice(
                invoiceNumber,
                appointment,
                treatmentCost,
                consultationFee,
                totalAmount,
                PaymentStatus.PENDING
        );

        Invoice saved = invoiceRepository.save(invoice);
        return mapToResponse(saved);
    }

    public InvoiceResponse markAsPaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        invoice.setPaymentStatus(PaymentStatus.PAID);
        Invoice updated = invoiceRepository.save(invoice);
        InvoiceResponse response = mapToResponse(updated);
        emailService.sendPaymentReceipt(response, invoice.getAppointment().getPatient().getEmail());
        return response;
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        return mapToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByAppointment(String appointmentNumber) {
        Invoice invoice = invoiceRepository.findByAppointmentAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for appointment number: " + appointmentNumber));
        return mapToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getPendingInvoices() {
        return invoiceRepository.findByPaymentStatus(PaymentStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateInvoiceNumber() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = invoiceRepository.count() + 1;
        String candidateNumber = "INV-" + dateStr + "-" + String.format("%03d", sequence);

        while (invoiceRepository.existsByInvoiceNumber(candidateNumber)) {
            sequence++;
            candidateNumber = "INV-" + dateStr + "-" + String.format("%03d", sequence);
        }
        return candidateNumber;
    }

    public InvoiceResponse mapToResponse(Invoice inv) {
        return new InvoiceResponse(
                inv.getId(),
                inv.getInvoiceNumber(),
                inv.getAppointment().getAppointmentNumber(),
                inv.getAppointment().getPatient().getId(),
                inv.getAppointment().getPatient().getFullName(),
                inv.getAppointment().getDentist().getFullName(),
                inv.getAppointment().getTreatmentType(),
                inv.getTreatmentCost(),
                inv.getConsultationFee(),
                inv.getTotalAmount(),
                inv.getPaymentStatus(),
                inv.getGeneratedDate()
        );
    }
}
