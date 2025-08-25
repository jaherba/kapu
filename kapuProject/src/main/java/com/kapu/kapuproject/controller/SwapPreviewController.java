package com.kapu.kapuproject.controller;

import com.kapu.kapuproject.model.Shift;
import com.kapu.kapuproject.model.ShiftSwapRequest;
import com.kapu.kapuproject.model.ShiftType;
import com.kapu.kapuproject.service.ShiftService;
import com.kapu.kapuproject.service.ShiftSwapRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SwapPreviewController {

    @Autowired
    private ShiftSwapRequestService shiftSwapRequestService;

    @Autowired
    private ShiftService shiftService;

    @GetMapping("/swap-preview/{id}")
    public String previewSwap(@PathVariable("id") Long id,
                              @RequestParam(name = "mode", defaultValue = "simplified") String mode,
                              Model model,
                              Principal principal,
                              HttpServletRequest request) {

        Optional<ShiftSwapRequest> optional = shiftSwapRequestService.getById(id);
        if (optional.isEmpty()) {
            return "redirect:/notifications";
        }

        ShiftSwapRequest requestObj = optional.get();
        Shift fromShift = requestObj.getFromShift();
        Shift toShift = requestObj.getToShift();

        LocalDate date1 = fromShift.getDate();
        LocalDate date2 = toShift.getDate();

        LocalDate earlier = date1.isBefore(date2) ? date1 : date2;
        LocalDate later = date1.isAfter(date2) ? date1 : date2;

        Set<LocalDate> datesToShow;

        if (mode.equals("simplified")) {
            datesToShow = new TreeSet<>();
            long daysBetween = ChronoUnit.DAYS.between(earlier, later);
            if (daysBetween > 1) {
                datesToShow.add(earlier.minusDays(1));
                datesToShow.add(earlier);
                datesToShow.add(earlier.plusDays(1));
                datesToShow.add(later.minusDays(1));
                datesToShow.add(later);
                datesToShow.add(later.plusDays(1));
            } else {
                for (LocalDate d = earlier.minusDays(1); !d.isAfter(later.plusDays(1)); d = d.plusDays(1)) {
                    datesToShow.add(d);
                }
            }
        } else {
            List<Shift> allShifts = shiftService.getAllShifts();
            datesToShow = allShifts.stream()
                    .map(Shift::getDate)
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        List<Shift> allShifts = shiftService.getAllShifts();

        Map<LocalDate, List<Shift>> shiftsByDate = allShifts.stream()
                .filter(s -> datesToShow.contains(s.getDate()))
                .collect(Collectors.groupingBy(Shift::getDate));

        List<ShiftType> orderedShiftTypes = Arrays.asList(
                ShiftType.NIGHT_SHIFT_SF, ShiftType.NIGHT_SHIFT, ShiftType.NIGHT_SHIFT_PLUS,
                ShiftType.MORNING_SF, ShiftType.MORNING_REZE, ShiftType.MORNING_CAFE_1, ShiftType.MORNING_CAFE_2,
                ShiftType.TS_1, ShiftType.TS_2, ShiftType.SEPP,
                ShiftType.AFTERNOON_SF, ShiftType.AFTERNOON_REZE, ShiftType.AFTERNOON_CAFE, ShiftType.AFTERNOON_GASTRO,
                ShiftType.AS_1, ShiftType.AS_2, ShiftType.EXTRA
        );

        model.addAttribute("shiftsByDate", shiftsByDate);
        model.addAttribute("orderedShiftTypes", orderedShiftTypes);
        model.addAttribute("fromShiftId", fromShift.getId());
        model.addAttribute("toShiftId", toShift.getId());
        model.addAttribute("mode", mode);
        model.addAttribute("requestId", id);

        return "swap-preview";
    }
}
