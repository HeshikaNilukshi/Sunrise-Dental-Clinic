package com.sunrisedental.dental_clinic.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.sunrisedental.dental_clinic.dto.AppointmentResponse;
import com.sunrisedental.dental_clinic.dto.InvoiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${resend.api.key:}")
    private String apiKey;

    @Value("${resend.from.email:onboarding@resend.dev}")
    private String fromEmail;

    @Async
    public void sendBookingConfirmation(AppointmentResponse appointment) {
        if (appointment.getPatientEmail() == null || appointment.getPatientEmail().isBlank()) {
            return;
        }

        if (!isConfigured()) {
            log.info("Email service not configured. Skipping booking confirmation for: {}", appointment.getAppointmentNumber());
            return;
        }

        try {
            Resend resend = new Resend(apiKey);
            String htmlContent = buildBookingConfirmationHtml(appointment);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(appointment.getPatientEmail())
                    .subject("Appointment Confirmed - Sunrise Dental Clinic (" + appointment.getAppointmentNumber() + ")")
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
            log.info("Booking confirmation email sent for appointment: {}", appointment.getAppointmentNumber());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email for appointment {}: {}",
                    appointment.getAppointmentNumber(), e.getMessage());
        }
    }

    @Async
    public void sendCancellationNotice(AppointmentResponse appointment) {
        if (appointment.getPatientEmail() == null || appointment.getPatientEmail().isBlank()) {
            return;
        }

        if (!isConfigured()) {
            log.info("Email service not configured. Skipping cancellation notice for: {}", appointment.getAppointmentNumber());
            return;
        }

        try {
            Resend resend = new Resend(apiKey);
            String htmlContent = buildCancellationNoticeHtml(appointment);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(appointment.getPatientEmail())
                    .subject("Appointment Cancelled - Sunrise Dental Clinic (" + appointment.getAppointmentNumber() + ")")
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
            log.info("Cancellation notice email sent for appointment: {}", appointment.getAppointmentNumber());
        } catch (Exception e) {
            log.error("Failed to send cancellation notice email for appointment {}: {}",
                    appointment.getAppointmentNumber(), e.getMessage());
        }
    }

    @Async
    public void sendPaymentReceipt(InvoiceResponse invoice, String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        if (!isConfigured()) {
            log.info("Email service not configured. Skipping receipt email for: {}", invoice.getInvoiceNumber());
            return;
        }

        try {
            Resend resend = new Resend(apiKey);
            String htmlContent = buildPaymentReceiptHtml(invoice);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(recipientEmail)
                    .subject("Payment Receipt - Sunrise Dental Clinic (" + invoice.getInvoiceNumber() + ")")
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
            log.info("Payment receipt email sent for invoice: {}", invoice.getInvoiceNumber());
        } catch (Exception e) {
            log.error("Failed to send payment receipt email for invoice {}: {}",
                    invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    private boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("re_your_api_key");
    }

    private String buildBookingConfirmationHtml(AppointmentResponse apt) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;'>"
                + "<h2 style='color: #0284c7; margin-top: 0;'>Sunrise Dental Clinic</h2>"
                + "<h3 style='color: #1e293b;'>Appointment Estimate</h3>"
                + "<p>Dear <strong>" + apt.getPatientName() + "</strong>,</p>"
                + "<p>Your dental appointment has been successfully scheduled. Details and estimate are below:</p>"
                + "<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Appointment Number:</td><td style='padding: 8px 0; font-weight: bold;'>" + apt.getAppointmentNumber() + "</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Date & Time:</td><td style='padding: 8px 0; font-weight: bold;'>" + apt.getAppointmentDate() + " at " + apt.getAppointmentTime() + "</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Dentist:</td><td style='padding: 8px 0; font-weight: bold;'>" + apt.getDentistName() + " (" + apt.getDentistSpecialization() + ")</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Treatment Type:</td><td style='padding: 8px 0; font-weight: bold;'>" + apt.getTreatmentType() + "</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Treatment Cost:</td><td style='padding: 8px 0; font-weight: bold;'>LKR " + apt.getTreatmentCost() + "</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Consultation Fee:</td><td style='padding: 8px 0; font-weight: bold;'>LKR " + apt.getConsultationFee() + "</td></tr>"
                + "<tr style='border-top: 2px solid #e2e8f0;'><td style='padding: 12px 0; font-weight: bold;'>Total Estimated Cost:</td><td style='padding: 12px 0; font-weight: bold; color: #0284c7;'>LKR " + apt.getTotalCost() + "</td></tr>"
                + "</table>"
                + "<p style='color: #64748b; font-size: 14px;'>Location: Sunrise Dental Clinic, Colombo, Sri Lanka</p>"
                + "<p style='color: #94a3b8; font-size: 12px; margin-top: 24px;'>If you need to reschedule or cancel, please contact the clinic.</p>"
                + "</div>";
    }

    private String buildCancellationNoticeHtml(AppointmentResponse apt) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;'>"
                + "<h2 style='color: #dc2626; margin-top: 0;'>Sunrise Dental Clinic</h2>"
                + "<h3 style='color: #1e293b;'>Appointment Cancellation Notice</h3>"
                + "<p>Dear <strong>" + apt.getPatientName() + "</strong>,</p>"
                + "<p>Your appointment <strong>" + apt.getAppointmentNumber() + "</strong> scheduled on <strong>"
                + apt.getAppointmentDate() + " at " + apt.getAppointmentTime() + "</strong> with <strong>"
                + apt.getDentistName() + "</strong> has been cancelled.</p>"
                + "<p style='color: #64748b; font-size: 14px;'>If this was done in error or you wish to rebook, please contact our reception.</p>"
                + "</div>";
    }

    private String buildPaymentReceiptHtml(InvoiceResponse inv) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;'>"
                + "<h2 style='color: #16a34a; margin-top: 0;'>Sunrise Dental Clinic</h2>"
                + "<h3 style='color: #1e293b;'>Payment Receipt</h3>"
                + "<p>Dear <strong>" + inv.getPatientName() + "</strong>,</p>"
                + "<p>Thank you for your payment. Details of your receipt are below:</p>"
                + "<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Invoice Number:</td><td style='padding: 8px 0; font-weight: bold;'>" + inv.getInvoiceNumber() + "</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Appointment Number:</td><td style='padding: 8px 0; font-weight: bold;'>" + inv.getAppointmentNumber() + "</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Dentist:</td><td style='padding: 8px 0; font-weight: bold;'>" + inv.getDentistName() + "</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Treatment Type:</td><td style='padding: 8px 0; font-weight: bold;'>" + inv.getTreatmentType() + "</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Treatment Cost:</td><td style='padding: 8px 0; font-weight: bold;'>LKR " + inv.getTreatmentCost() + "</td></tr>"
                + "<tr><td style='padding: 8px 0; color: #64748b;'>Consultation Fee:</td><td style='padding: 8px 0; font-weight: bold;'>LKR " + inv.getConsultationFee() + "</td></tr>"
                + "<tr style='border-top: 2px solid #e2e8f0;'><td style='padding: 12px 0; font-weight: bold;'>Total Paid:</td><td style='padding: 12px 0; font-weight: bold; color: #16a34a;'>LKR " + inv.getTotalAmount() + "</td></tr>"
                + "</table>"
                + "<p style='color: #64748b; font-size: 14px;'>Status: " + inv.getPaymentStatus() + "</p>"
                + "</div>";
    }
}
