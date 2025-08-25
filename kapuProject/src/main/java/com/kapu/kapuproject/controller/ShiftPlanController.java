package com.kapu.kapuproject.controller;

import com.kapu.kapuproject.model.*;
import com.kapu.kapuproject.service.ShiftService;
import com.kapu.kapuproject.service.UserService;
import com.kapu.kapuproject.service.ShiftSwapRequestService;
import com.kapu.kapuproject.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.security.Principal;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/shiftplan")
public class ShiftPlanController {

    private final ShiftService shiftService;
    private final UserService userService;
    private final ShiftSwapRequestService shiftSwapRequestService;
    
    @Autowired
    private HttpSession httpSession;
    
    @Autowired
    private NotificationService notificationService;


    @Autowired
    public ShiftPlanController(ShiftService shiftService, UserService userService, ShiftSwapRequestService shiftSwapRequestService) {
        this.shiftService = shiftService;
        this.userService = userService;
        this.shiftSwapRequestService = shiftSwapRequestService;
    }

    @GetMapping
    public String getShiftPlan(Model model, Principal principal) {
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

        model.addAttribute("shiftsByDate", shiftsByDate);
        model.addAttribute("orderedShiftTypes", orderedShiftTypes);

        if (principal != null) {
            String email = principal.getName();
            Optional<User> currentUserOpt = userService.getUserByEmail(email);

            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                
                // 🔔 Añadir contador de notificaciones no leídas
                List<Notification> unreadNotifications = notificationService.getUnreadNotifications(currentUser);
                model.addAttribute("unreadNotificationCount", unreadNotifications.size());


                List<ShiftSwapRequest> pendingRequests = shiftSwapRequestService
                    .getRequestsByRecipient(currentUser)
                    .stream()
                    .filter(r -> r.getStatus() == Status.PENDING)
                    .toList();

                model.addAttribute("pendingRequests", pendingRequests);
                model.addAttribute("currentUser", currentUserOpt.get());
            }
        }
        
        Object confirmationMessage = httpSession.getAttribute("swapConfirmation");
        if (confirmationMessage != null) {
            model.addAttribute("confirmationMessage", confirmationMessage.toString());
            httpSession.removeAttribute("swapConfirmation");
        }

        return "shiftplan";
    }
    
    

    @PostMapping("/swap")
    @ResponseBody
    public ResponseEntity<String> requestSwap(@RequestBody Map<String, Long> payload, Principal principal) {
        Long id1 = payload.get("fromShiftId");
        Long id2 = payload.get("toShiftId");

        Optional<Shift> optional1 = shiftService.getShiftById(id1);
        Optional<Shift> optional2 = shiftService.getShiftById(id2);

        if (optional1.isPresent() && optional2.isPresent()) {
            Shift shift1 = optional1.get();
            Shift shift2 = optional2.get();

            User user1 = shift1.getEmployee();
            User user2 = shift2.getEmployee();

            if (user1 == null || user2 == null) {
                return ResponseEntity.badRequest().body("Both shifts must have assigned users");
            }

            String loggedInEmail = principal.getName();

            if (!user1.getEmail().equals(loggedInEmail) && !user2.getEmail().equals(loggedInEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not part of this swap");
            }

            boolean shift1SL = shift1.getName().isShiftLeaderOnly();
            boolean shift2SL = shift2.getName().isShiftLeaderOnly();

            if ((shift1SL || shift2SL) && (!user1.isShiftLeader() || !user2.isShiftLeader())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Both users must be shift leaders");
            }

            User requester = user1.getEmail().equals(loggedInEmail) ? user1 : user2;
            User recipient = requester == user1 ? user2 : user1;

            ShiftSwapRequest request = new ShiftSwapRequest();
            request.setFromShift(shift1);
            request.setToShift(shift2);
            request.setRequester(requester);
            request.setRecipient(recipient);
            request.setStatus(Status.PENDING);

            shiftSwapRequestService.saveRequest(request);

            return ResponseEntity.ok("Swap request created and pending approval");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("One or both shifts not found");
    }

    @PostMapping("/swap/accept/{id}")
    @ResponseBody
    public ResponseEntity<String> acceptSwap(@PathVariable("id") Long id, Principal principal) {
        Optional<ShiftSwapRequest> optional = shiftSwapRequestService.getById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Request not found");
        }

        ShiftSwapRequest request = optional.get();

        if (!request.getStatus().equals(Status.PENDING)) {
            return ResponseEntity.badRequest().body("Request already handled");
        }

        if (!request.getRecipient().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
        }

        // Swap employees
        Shift from = request.getFromShift();
        Shift to = request.getToShift();

        User requester = request.getRequester();
        User recipient = request.getRecipient();

        User temp = from.getEmployee();
        from.setEmployee(to.getEmployee());
        to.setEmployee(temp);

        shiftService.saveShift(from);
        shiftService.saveShift(to);

        request.setStatus(Status.ACCEPTED);
        shiftSwapRequestService.saveRequest(request);

        // Create a notification for the requester
        String message = "Your shift '" + from.getName() + " on " + from.getDate()
                       + "' was successfully swapped with " + recipient.getName()
                       + ". You now have '" + to.getName() + " on " + to.getDate() + "'.";

        notificationService.createNotification(
            requester,
            message,
            NotificationType.SUCCESS,
            "/shiftplan"
        );

        return ResponseEntity.ok("Swap accepted and shifts updated");
    }



    @PostMapping("/swap/reject/{id}")
    @ResponseBody
    public ResponseEntity<String> rejectSwap(@PathVariable("id") Long id, Principal principal) {
        Optional<ShiftSwapRequest> optional = shiftSwapRequestService.getById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Request not found");
        }

        ShiftSwapRequest request = optional.get();

        if (!request.getStatus().equals(Status.PENDING)) {
            return ResponseEntity.badRequest().body("Request already handled");
        }

        if (!request.getRecipient().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
        }

        request.setStatus(Status.REJECTED);
        shiftSwapRequestService.saveRequest(request);

        return ResponseEntity.ok("Swap request rejected");
    }
}
