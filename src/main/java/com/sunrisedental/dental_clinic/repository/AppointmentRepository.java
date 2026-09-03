package com.sunrisedental.dental_clinic.repository;

import com.sunrisedental.dental_clinic.model.Appointment;
import com.sunrisedental.dental_clinic.model.Dentist;
import com.sunrisedental.dental_clinic.model.Patient;
import com.sunrisedental.dental_clinic.model.enums.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentNumber(String appointmentNumber);

    List<Appointment> findByDentistAndAppointmentDateAndStatusNot(Dentist dentist, LocalDate appointmentDate, AppointmentStatus status);

    boolean existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusNot(Dentist dentist, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status);

    List<Appointment> findByAppointmentDateOrderByIdDesc(LocalDate appointmentDate);

    List<Appointment> findAllByOrderByIdDesc();

    List<Appointment> findByPatient(Patient patient);

    List<Appointment> findByDentist(Dentist dentist);

    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId);

    long countByAppointmentDate(LocalDate appointmentDate);

    boolean existsByAppointmentNumber(String appointmentNumber);
}
