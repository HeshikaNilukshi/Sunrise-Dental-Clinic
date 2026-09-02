package com.sunrisedental.dental_clinic.controller;

import com.sunrisedental.dental_clinic.dto.InvoiceResponse;
import com.sunrisedental.dental_clinic.service.BillingService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final BillingService billingService;

    public InvoiceController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/generate/{appointmentNumber}")
    public ResponseEntity<InvoiceResponse> generateInvoice(@PathVariable String appointmentNumber) {
        InvoiceResponse response = billingService.generateInvoice(appointmentNumber);
        return ResponseEntity.created(URI.create("/api/invoices/" + response.getId())).body(response);
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponse> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.markAsPaid(id));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InvoiceResponse>> getPendingInvoices() {
        return ResponseEntity.ok(billingService.getPendingInvoices());
    }

    @GetMapping("/appointment/{appointmentNumber}")
    public ResponseEntity<InvoiceResponse> getInvoiceByAppointment(@PathVariable String appointmentNumber) {
        return ResponseEntity.ok(billingService.getInvoiceByAppointment(appointmentNumber));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoiceById(id));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(billingService.getAllInvoices());
    }
}
