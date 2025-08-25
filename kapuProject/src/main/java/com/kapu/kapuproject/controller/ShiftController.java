package com.kapu.kapuproject.controller;

import com.kapu.kapuproject.model.Shift;
import com.kapu.kapuproject.model.ShiftType;
import com.kapu.kapuproject.model.User;
import com.kapu.kapuproject.service.ShiftService;
import com.kapu.kapuproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/shifts")
public class ShiftController {

    private final ShiftService shiftService;
    private final UserService userService;

    @Autowired
    public ShiftController(ShiftService shiftService, UserService userService) {
        this.shiftService = shiftService;
        this.userService = userService;
    }

    @GetMapping
    public String getAllShifts(Model model) {
        List<Shift> shifts = shiftService.getAllShifts();
        model.addAttribute("shifts", shifts);
        return "shifts";
    }

    @GetMapping("/filter")
    public String getShiftsByName(@RequestParam ShiftType name, Model model) {
        List<Shift> shifts = shiftService.getShiftsByName(name);
        model.addAttribute("shifts", shifts);
        return "shifts";
    }

    @GetMapping("/date")
    public String getShiftsByDate(@RequestParam String date, Model model) {  // 📌 Nuevo método para buscar turnos por fecha
        LocalDate shiftDate = LocalDate.parse(date);
        List<Shift> shifts = shiftService.getShiftsByDate(shiftDate);
        model.addAttribute("shifts", shifts);
        return "shifts";
    }

    @GetMapping("/new")
    public String showCreateShiftForm(Model model) {
        model.addAttribute("shift", new Shift());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("shiftTypes", ShiftType.values());
        return "create-shift";
    }

    @PostMapping
    public String saveShift(@ModelAttribute Shift shift, @RequestParam(name = "employeeId", required = false) Long employeeId) {
        if (employeeId != null) {
            Optional<User> employee = userService.getUserById(employeeId);
            employee.ifPresent(shift::setEmployee);
        } else {
            shift.setEmployee(null);
        }
        shiftService.saveShift(shift);
        return "redirect:/shifts";
    }

    @GetMapping("/delete/{id}")
    public String deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return "redirect:/shifts";
    }
}
