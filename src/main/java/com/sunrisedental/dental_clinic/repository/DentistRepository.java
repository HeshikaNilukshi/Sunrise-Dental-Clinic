package com.sunrisedental.dental_clinic.repository;

import com.sunrisedental.dental_clinic.model.Dentist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DentistRepository extends JpaRepository<Dentist, Long> {

    List<Dentist> findByActiveTrue();

    List<Dentist> findBySpecialization(String specialization);
}
