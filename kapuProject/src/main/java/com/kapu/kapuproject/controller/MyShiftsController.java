package com.kapu.kapuproject.controller;

import com.kapu.kapuproject.model.*;
import com.kapu.kapuproject.service.ShiftService;
import com.kapu.kapuproject.service.UserService;
import com.kapu.kapuproject.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/my-shifts")
public class MyShiftsController {

    private final ShiftService shiftService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public MyShiftsController(ShiftService shiftService, UserService userService, NotificationService notificationService) {
        this.shiftService = shiftService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String getMyShifts(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String email = principal.getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) return "redirect:/login";

        User user = userOpt.get();

        List<Shift> allShifts = shiftService.getAllShifts();

        // 1. Obtain dates for witch the employee has an asigned shift
        Set<LocalDate> myShiftDates = allShifts.stream()
                .filter(shift -> shift.getEmployee() != null && shift.getEmployee().getId().equals(user.getId()))
                .map(Shift::getDate)
                .collect(Collectors.toSet());

        // 2. Filter all shifts but only in those dates
        Map<LocalDate, List<Shift>> shiftsByDate = allShifts.stream()
                .filter(shift -> myShiftDates.contains(shift.getDate()))
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

        // 🔔 Unread notifications
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(user);
        model.addAttribute("unreadNotificationCount", unreadNotifications.size());
        
        model.addAttribute("currentUser", user);


        return "my-shifts";
    }
}
