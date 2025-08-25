package com.kapu.kapuproject.controller;

import com.kapu.kapuproject.model.Shift;
import com.kapu.kapuproject.model.ShiftType;
import com.kapu.kapuproject.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/testShiftPlan")
public class TestShiftPlanController {

    private final ShiftService shiftService;

    @Autowired
    public TestShiftPlanController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public String getTestShiftPlan(Model model) {
        // Obtain all shifts from database
        List<Shift> shifts = shiftService.getAllShifts();

        // order by date (ascending)
        Map<LocalDate, List<Shift>> shiftsByDate = shifts.stream()
                .collect(Collectors.groupingBy(Shift::getDate));

        // define the order of shift types
        List<ShiftType> orderedShiftTypes = Arrays.asList(
                ShiftType.NIGHT_SHIFT_SF, ShiftType.NIGHT_SHIFT, ShiftType.NIGHT_SHIFT_PLUS,
                ShiftType.MORNING_SF, ShiftType.MORNING_REZE, ShiftType.MORNING_CAFE_1, ShiftType.MORNING_CAFE_2,
                ShiftType.TS_1, ShiftType.TS_2, ShiftType.SEPP,
                ShiftType.AFTERNOON_SF, ShiftType.AFTERNOON_REZE, ShiftType.AFTERNOON_CAFE, ShiftType.AFTERNOON_GASTRO,
                ShiftType.AS_1, ShiftType.AS_2, ShiftType.EXTRA
        );

        model.addAttribute("shiftsByDate", shiftsByDate);
        model.addAttribute("orderedShiftTypes", orderedShiftTypes);

        return "testShiftPlan"; 
    }
}
