package com.sunrisedental.dental_clinic.repository;

import com.sunrisedental.dental_clinic.model.Patient;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByContactNumber(String contactNumber);

    List<Patient> findAllByOrderByIdDesc();

    List<Patient> findByFullNameContainingIgnoreCaseOrderByIdDesc(String fullName);

    boolean existsByContactNumber(String contactNumber);
}
