package com.sunrisedental.dental_clinic.controller;

import com.sunrisedental.dental_clinic.dto.AppointmentResponse;
import com.sunrisedental.dental_clinic.dto.DentistResponse;
import com.sunrisedental.dental_clinic.dto.InvoiceResponse;
import com.sunrisedental.dental_clinic.dto.PatientResponse;
import com.sunrisedental.dental_clinic.model.enums.AppointmentStatus;
import com.sunrisedental.dental_clinic.model.enums.PaymentStatus;
import com.sunrisedental.dental_clinic.model.enums.TreatmentType;
import com.sunrisedental.dental_clinic.service.AppointmentService;
import com.sunrisedental.dental_clinic.service.BillingService;
import com.sunrisedental.dental_clinic.service.DentistService;
import com.sunrisedental.dental_clinic.service.PatientService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sunrisedental.dental_clinic.dto.AppointmentRequest;
import com.sunrisedental.dental_clinic.dto.DentistRequest;
import com.sunrisedental.dental_clinic.dto.PatientRequest;
import com.sunrisedental.dental_clinic.exception.DuplicateBookingException;
import com.sunrisedental.dental_clinic.exception.DuplicateResourceException;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {

    private final PatientService patientService;
    private final DentistService dentistService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;

    public PageController(PatientService patientService,
                          DentistService dentistService,
                          AppointmentService appointmentService,
                          BillingService billingService) {
        this.patientService = patientService;
        this.dentistService = dentistService;
        this.appointmentService = appointmentService;
        this.billingService = billingService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        List<AppointmentResponse> todayAppointments = appointmentService.getAppointmentsByDate(today);
        long scheduledCount = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .count();
        long completedCount = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();

        List<PatientResponse> allPatients = patientService.getAllPatients();
        List<PatientResponse> recentPatients = allPatients.stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .limit(5)
                .collect(Collectors.toList());

        List<DentistResponse> activeDentists = dentistService.getActiveDentists();
        List<InvoiceResponse> pendingInvoices = billingService.getPendingInvoices();

        model.addAttribute("today", today);
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("todayAppointmentsCount", todayAppointments.size());
        model.addAttribute("scheduledCount", scheduledCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalPatientsCount", allPatients.size());
        model.addAttribute("recentPatients", recentPatients);
        model.addAttribute("activeDentistsCount", activeDentists.size());
        model.addAttribute("pendingInvoicesCount", pendingInvoices.size());
        model.addAttribute("treatmentTypes", TreatmentType.values());
        model.addAttribute("pageTitle", "Dashboard");

        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/help")
    public String helpPage(Model model) {
        model.addAttribute("pageTitle", "Help & System Guide");
        return "help";
    }

    @GetMapping("/patients")
    public String listPatients(@RequestParam(required = false) String search, Model model) {
        List<PatientResponse> patients;
        if (search != null && !search.trim().isEmpty()) {
            patients = patientService.searchByName(search.trim());
        } else {
            patients = patientService.getAllPatients();
        }
        model.addAttribute("patients", patients);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Patients Directory");
        return "patients/list";
    }

    @GetMapping("/patients/new")
    public String newPatientForm(Model model) {
        model.addAttribute("patientRequest", new PatientRequest());
        model.addAttribute("pageTitle", "Register New Patient");
        return "patients/form";
    }

    @PostMapping("/patients")
    public String registerPatient(@Valid @ModelAttribute("patientRequest") PatientRequest request,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Register New Patient");
            return "patients/form";
        }
        try {
            PatientResponse saved = patientService.registerPatient(request);
            redirectAttributes.addFlashAttribute("successMessage", "Patient " + saved.getFullName() + " registered successfully!");
            return "redirect:/patients/" + saved.getId();
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue("contactNumber", "duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Register New Patient");
            return "patients/form";
        }
    }

    @GetMapping("/patients/{id}")
    public String patientDetail(@PathVariable Long id, Model model) {
        PatientResponse patient = patientService.getPatientById(id);
        List<AppointmentResponse> appointments = appointmentService.getPatientHistory(id);
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("pageTitle", patient.getFullName() + " - Patient Profile");
        return "patients/detail";
    }

    @GetMapping("/dentists")
    public String listDentists(Model model) {
        List<DentistResponse> dentists = dentistService.getAllDentists();
        model.addAttribute("dentists", dentists);
        model.addAttribute("pageTitle", "Dentists Roster");
        return "dentists/list";
    }

    @GetMapping("/dentists/new")
    public String newDentistForm(Model model) {
        model.addAttribute("dentistRequest", new DentistRequest());
        model.addAttribute("pageTitle", "Add New Dentist");
        return "dentists/form";
    }

    @PostMapping("/dentists")
    public String createDentist(@Valid @ModelAttribute("dentistRequest") DentistRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Dentist");
            return "dentists/form";
        }
        DentistResponse saved = dentistService.createDentist(request);
        redirectAttributes.addFlashAttribute("successMessage", "Dentist " + saved.getFullName() + " added to clinic roster!");
        return "redirect:/dentists";
    }

    @GetMapping("/appointments")
    public String listAppointments(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   Model model) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDate(queryDate);
        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedDate", queryDate);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("pageTitle", "Appointments Schedule");
        return "appointments/list";
    }

    @GetMapping("/appointments/new")
    public String newAppointmentForm(@RequestParam(required = false) Long patientId,
                                     @RequestParam(required = false) Long dentistId,
                                     Model model) {
        AppointmentRequest request = new AppointmentRequest();
        if (patientId != null) request.setPatientId(patientId);
        if (dentistId != null) request.setDentistId(dentistId);
        request.setAppointmentDate(LocalDate.now());

        model.addAttribute("appointmentRequest", request);
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("dentists", dentistService.getActiveDentists());
        model.addAttribute("treatmentTypes", TreatmentType.values());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("pageTitle", "Book Appointment");
        return "appointments/form";
    }

    @PostMapping("/appointments")
    public String bookAppointment(@Valid @ModelAttribute("appointmentRequest") AppointmentRequest request,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("dentists", dentistService.getActiveDentists());
            model.addAttribute("treatmentTypes", TreatmentType.values());
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("pageTitle", "Book Appointment");
            return "appointments/form";
        }
        try {
            AppointmentResponse saved = appointmentService.bookAppointment(request);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment " + saved.getAppointmentNumber() + " scheduled successfully!");
            return "redirect:/appointments/" + saved.getAppointmentNumber();
        } catch (DuplicateBookingException | IllegalStateException e) {
            model.addAttribute("bookingError", e.getMessage());
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("dentists", dentistService.getActiveDentists());
            model.addAttribute("treatmentTypes", TreatmentType.values());
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("pageTitle", "Book Appointment");
            return "appointments/form";
        }
    }

    @GetMapping("/appointments/{number}")
    public String appointmentDetail(@PathVariable String number, Model model) {
        AppointmentResponse appointment = appointmentService.getByAppointmentNumber(number);
        InvoiceResponse invoice = null;
        try {
            invoice = billingService.getInvoiceByAppointment(number);
        } catch (Exception ignored) {
        }
        model.addAttribute("appointment", appointment);
        model.addAttribute("invoice", invoice);
        model.addAttribute("pageTitle", "Appointment " + number);
        return "appointments/detail";
    }

    @PostMapping("/appointments/{number}/complete")
    public String completeAppointment(@PathVariable String number, RedirectAttributes redirectAttributes) {
        appointmentService.completeAppointment(number);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment " + number + " marked as completed!");
        return "redirect:/appointments/" + number;
    }

    @PostMapping("/appointments/{number}/cancel")
    public String cancelAppointment(@PathVariable String number, RedirectAttributes redirectAttributes) {
        appointmentService.cancelAppointment(number);
        redirectAttributes.addFlashAttribute("infoMessage", "Appointment " + number + " has been cancelled.");
        return "redirect:/appointments/" + number;
    }

    @GetMapping("/appointments/search")
    public String searchAppointment(@RequestParam(required = false) String appointmentNumber, Model model) {
        if (appointmentNumber != null && !appointmentNumber.trim().isEmpty()) {
            try {
                appointmentService.getByAppointmentNumber(appointmentNumber.trim());
                return "redirect:/appointments/" + appointmentNumber.trim();
            } catch (Exception e) {
                model.addAttribute("notFound", true);
                model.addAttribute("appointmentNumber", appointmentNumber.trim());
            }
        }
        model.addAttribute("pageTitle", "Search Appointment");
        return "appointments/search";
    }

    @GetMapping("/invoices")
    public String listInvoices(@RequestParam(required = false) String status, Model model) {
        List<InvoiceResponse> allInvoices = billingService.getAllInvoices();
        List<InvoiceResponse> filtered;
        if ("PENDING".equalsIgnoreCase(status)) {
            filtered = allInvoices.stream()
                    .filter(i -> i.getPaymentStatus() == PaymentStatus.PENDING)
                    .collect(Collectors.toList());
        } else if ("PAID".equalsIgnoreCase(status)) {
            filtered = allInvoices.stream()
                    .filter(i -> i.getPaymentStatus() == PaymentStatus.PAID)
                    .collect(Collectors.toList());
        } else {
            filtered = allInvoices;
        }

        BigDecimal totalInvoiced = allInvoices.stream()
                .map(InvoiceResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = allInvoices.stream()
                .filter(i -> i.getPaymentStatus() == PaymentStatus.PAID)
                .map(InvoiceResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPending = allInvoices.stream()
                .filter(i -> i.getPaymentStatus() == PaymentStatus.PENDING)
                .map(InvoiceResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingCount = allInvoices.stream()
                .filter(i -> i.getPaymentStatus() == PaymentStatus.PENDING)
                .count();
        long paidCount = allInvoices.stream()
                .filter(i -> i.getPaymentStatus() == PaymentStatus.PAID)
                .count();

        model.addAttribute("invoices", filtered);
        model.addAttribute("allInvoices", allInvoices);
        model.addAttribute("totalInvoiced", totalInvoiced);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("selectedStatus", status != null ? status.toUpperCase() : "ALL");
        model.addAttribute("pageTitle", "Billing & Invoices");
        return "invoices/list";
    }

    @PostMapping("/invoices/generate/{appointmentNumber}")
    public String generateInvoice(@PathVariable String appointmentNumber, RedirectAttributes redirectAttributes) {
        InvoiceResponse response = billingService.generateInvoice(appointmentNumber);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice " + response.getInvoiceNumber() + " generated successfully!");
        return "redirect:/invoices/" + response.getId();
    }

    @GetMapping("/invoices/{id}")
    public String invoiceDetail(@PathVariable Long id, Model model) {
        InvoiceResponse invoice = billingService.getInvoiceById(id);
        AppointmentResponse appointment = null;
        try {
            appointment = appointmentService.getByAppointmentNumber(invoice.getAppointmentNumber());
        } catch (Exception ignored) {
        }
        model.addAttribute("invoice", invoice);
        model.addAttribute("appointment", appointment);
        model.addAttribute("pageTitle", "Invoice " + invoice.getInvoiceNumber());
        return "invoices/detail";
    }

    @PostMapping("/invoices/{id}/pay")
    public String markInvoicePaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        InvoiceResponse invoice = billingService.markAsPaid(id);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice " + invoice.getInvoiceNumber() + " marked as PAID! Receipt email sent to patient.");
        return "redirect:/invoices/" + id;
    }
}
