package com.kapu.kapuproject.service;

import com.kapu.kapuproject.model.Shift;
import com.kapu.kapuproject.model.ShiftType;
import com.kapu.kapuproject.model.User;
import com.kapu.kapuproject.repository.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;

    @Autowired
    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public Shift saveShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public Optional<Shift> getShiftById(Long id) {
        return shiftRepository.findById(id);
    }

    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }

    public List<Shift> getShiftsByName(ShiftType name) {
        return shiftRepository.findByName(name);
    }

    public List<Shift> getShiftsByDate(LocalDate date) {  
        return shiftRepository.findByDate(date);
    }
}

