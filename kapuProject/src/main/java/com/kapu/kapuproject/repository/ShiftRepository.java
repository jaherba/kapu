package com.kapu.kapuproject.repository;

import com.kapu.kapuproject.model.Shift;
import com.kapu.kapuproject.model.ShiftType;
import com.kapu.kapuproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByEmployee(User employee);
    List<Shift> findByName(ShiftType name);
    List<Shift> findByDate(LocalDate date); 
}
