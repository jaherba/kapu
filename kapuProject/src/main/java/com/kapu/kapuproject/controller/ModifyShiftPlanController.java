package com.kapu.kapuproject.controller;

import com.kapu.kapuproject.model.*;
import com.kapu.kapuproject.service.ShiftService;
import com.kapu.kapuproject.service.UserService;
import com.kapu.kapuproject.service.NotificationService;

import jakarta.servlet.http.HttpSession;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/modify-shiftplan")
public class ModifyShiftPlanController {

    private final ShiftService shiftService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    public ModifyShiftPlanController(ShiftService shiftService, UserService userService, NotificationService notificationService) {
        this.shiftService = shiftService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String getModifyShiftPlan(Model model, Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        // ⛔ Block acces if its not "Reze"
        if (userOpt.isEmpty() || !"Reze".equals(userOpt.get().getName())) {
            return "redirect:/access-denied";
        }

        User currentUser = userOpt.get();

        List<Shift> shifts = shiftService.getAllShifts();
        Map<LocalDate, List<Shift>> shiftsByDate = shifts.stream()
                .collect(Collectors.groupingBy(Shift::getDate));

        List<ShiftType> orderedShiftTypes = Arrays.asList(
                ShiftType.NIGHT_SHIFT_SF, ShiftType.NIGHT_SHIFT, ShiftType.NIGHT_SHIFT_PLUS,
                ShiftType.MORNING_SF, ShiftType.MORNING_REZE, ShiftType.MORNING_CAFE_1, ShiftType.MORNING_CAFE_2,
                ShiftType.TS_1, ShiftType.TS_2, ShiftType.SEPP,
                ShiftType.AFTERNOON_SF, ShiftType.AFTERNOON_REZE, ShiftType.AFTERNOON_CAFE, ShiftType.AFTERNOON_GASTRO,
                ShiftType.AS_1, ShiftType.AS_2, ShiftType.EXTRA
        );

        // ✅ only employees
        List<User> employees = userService.getAllUsers().stream()
                .filter(user -> Boolean.TRUE.equals(user.getEmployee()))
                .collect(Collectors.toList());

        model.addAttribute("shiftsByDate", shiftsByDate);
        model.addAttribute("orderedShiftTypes", orderedShiftTypes);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("employeeList", employees);

        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(currentUser);
        model.addAttribute("unreadNotificationCount", unreadNotifications.size());

        return "modify-shiftplan";
    }
    
    
    @PostMapping("/update-employee")
    @ResponseBody
    public ResponseEntity<String> updateShiftEmployee(@RequestBody Map<String, String> payload) {
        Long shiftId = Long.valueOf(payload.get("shiftId"));
        String userName = payload.get("userName");

        Optional<Shift> shiftOpt = shiftService.getShiftById(shiftId);
        Optional<User> userOpt = userService.getAllUsers().stream()
            .filter(u -> u.getName().equalsIgnoreCase(userName) && Boolean.TRUE.equals(u.getEmployee()))
            .findFirst();

        if (shiftOpt.isPresent() && userOpt.isPresent()) {
            Shift shift = shiftOpt.get();
            User oldEmployee = shift.getEmployee();  // 👈 previous employee
            User newEmployee = userOpt.get();

            shift.setEmployee(newEmployee);
            shiftService.saveShift(shift);

            System.out.println("🟢 Shift updated successfully: " + shift.getId());
            System.out.println("🟢 Old employee: " + (oldEmployee != null ? oldEmployee.getName() : "null"));
            System.out.println("🟢 New employee: " + newEmployee.getName());

            try {
                if (oldEmployee != null && !oldEmployee.equals(newEmployee)) {
                    String message = String.format(
                        "The shift %s on %s from %s to %s has been changed from: %s to: %s.",
                        shift.getName(),
                        shift.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        shift.getStart().toString(),
                        shift.getEnd().toString(),
                        oldEmployee.getName(),
                        newEmployee.getName()
                    );

                    notificationService.createNotification(oldEmployee, message, NotificationType.FROM_REZE, null);
                    notificationService.createNotification(newEmployee, message, NotificationType.FROM_REZE, null);
                }
            } catch (Exception e) {
                e.printStackTrace(); // 🛠️ shows error in console
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create notification");
            }

            return ResponseEntity.ok("Shift updated");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid shift or user");
    }
    
    
    
}