package com.sunrisedental.dental_clinic.repository;

import com.sunrisedental.dental_clinic.model.Appointment;
import com.sunrisedental.dental_clinic.model.Invoice;
import com.sunrisedental.dental_clinic.model.enums.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByAppointment(Appointment appointment);

    Optional<Invoice> findByAppointmentAppointmentNumber(String appointmentNumber);

    List<Invoice> findByPaymentStatusOrderByIdDesc(PaymentStatus paymentStatus);

    List<Invoice> findAllByOrderByIdDesc();

    boolean existsByInvoiceNumber(String invoiceNumber);
}
