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
import com.sunrisedental.dental_clinic.dto.RegisterRequest;
import com.sunrisedental.dental_clinic.service.UserService;
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
    private final UserService userService;

    public PageController(PatientService patientService,
                          DentistService dentistService,
                          AppointmentService appointmentService,
                          BillingService billingService,
                          UserService userService) {
        this.patientService = patientService;
        this.dentistService = dentistService;
        this.appointmentService = appointmentService;
        this.billingService = billingService;
        this.userService = userService;
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
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been successfully signed out.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account created successfully! Please sign in.");
        }
        return "auth/login";
    }

    @GetMapping("/staff")
    public String listStaff(Model model) {
        model.addAttribute("staffList", userService.getAllStaff());
        model.addAttribute("pageTitle", "Staff Management");
        return "staff/list";
    }

    @GetMapping("/staff/new")
    public String newStaffForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
        model.addAttribute("pageTitle", "Register Staff");
        return "staff/form";
    }

    @PostMapping("/staff")
    public String registerStaff(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
            model.addAttribute("pageTitle", "Register Staff");
            return "staff/form";
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerRequest", "Passwords do not match");
            model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
            model.addAttribute("pageTitle", "Register Staff");
            return "staff/form";
        }

        try {
            userService.registerUser(registerRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Staff account registered successfully!");
            return "redirect:/staff";
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue("email", "error.registerRequest", e.getMessage());
            model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
            model.addAttribute("pageTitle", "Register Staff");
            return "staff/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to register account: " + e.getMessage());
            model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
            model.addAttribute("pageTitle", "Register Staff");
            return "staff/form";
        }
    }

    @GetMapping("/staff/{id}/edit")
    public String editStaffForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("updateRequest")) {
            com.sunrisedental.dental_clinic.dto.StaffResponse staff = userService.getStaffById(id);
            com.sunrisedental.dental_clinic.dto.UpdateStaffRequest updateRequest = new com.sunrisedental.dental_clinic.dto.UpdateStaffRequest();
            updateRequest.setFullName(staff.getFullName());
            updateRequest.setEmail(staff.getEmail());
            updateRequest.setRole(staff.getRole());
            model.addAttribute("updateRequest", updateRequest);
            model.addAttribute("staffId", staff.getId());
        }
        model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
        model.addAttribute("pageTitle", "Edit Staff");
        return "staff/edit";
    }

    @PostMapping("/staff/{id}")
    public String updateStaff(@PathVariable Long id,
                              @Valid @ModelAttribute("updateRequest") com.sunrisedental.dental_clinic.dto.UpdateStaffRequest updateRequest,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              java.security.Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
            model.addAttribute("pageTitle", "Edit Staff");
            model.addAttribute("staffId", id);
            return "staff/edit";
        }

        try {
            userService.updateStaff(id, updateRequest, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Staff account updated successfully!");
            return "redirect:/staff";
        } catch (org.springframework.security.access.AccessDeniedException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
            model.addAttribute("pageTitle", "Edit Staff");
            model.addAttribute("staffId", id);
            return "staff/edit";
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue("email", "error.updateRequest", e.getMessage());
            model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
            model.addAttribute("pageTitle", "Edit Staff");
            model.addAttribute("staffId", id);
            return "staff/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update account: " + e.getMessage());
            model.addAttribute("roles", com.sunrisedental.dental_clinic.model.enums.UserRole.values());
            model.addAttribute("pageTitle", "Edit Staff");
            model.addAttribute("staffId", id);
            return "staff/edit";
        }
    }

    @PostMapping("/staff/{id}/delete")
    public String deleteStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteStaff(id);
            redirectAttributes.addFlashAttribute("successMessage", "Staff account deleted successfully!");
        } catch (org.springframework.security.access.AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete account: " + e.getMessage());
        }
        return "redirect:/staff";
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

    @PostMapping("/patients/{id}/delete")
    public String deletePatient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        patientService.deletePatient(id);
        redirectAttributes.addFlashAttribute("successMessage", "Patient and associated records deleted successfully!");
        return "redirect:/patients";
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

    @PostMapping("/dentists/{id}/delete")
    public String deleteDentist(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        dentistService.deleteDentist(id);
        redirectAttributes.addFlashAttribute("successMessage", "Dentist and associated records deleted successfully!");
        return "redirect:/dentists";
    }

    @GetMapping("/appointments")
    public String listAppointments(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   Model model) {
        List<AppointmentResponse> appointments;
        if (date != null) {
            appointments = appointmentService.getAppointmentsByDate(date);
        } else {
            appointments = appointmentService.getAllAppointments();
        }
        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedDate", date);
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

    @PostMapping("/appointments/{number}/delete")
    public String deleteAppointment(@PathVariable String number, RedirectAttributes redirectAttributes) {
        appointmentService.deleteAppointment(number);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment " + number + " deleted successfully!");
        return "redirect:/appointments";
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

    @PostMapping("/invoices/{id}/delete")
    public String deleteInvoice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        billingService.deleteInvoice(id);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice deleted successfully!");
        return "redirect:/invoices";
    }
}
