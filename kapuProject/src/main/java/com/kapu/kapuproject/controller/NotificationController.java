package com.kapu.kapuproject.controller;

import com.kapu.kapuproject.model.Notification;
import com.kapu.kapuproject.model.NotificationType;
import com.kapu.kapuproject.model.ShiftSwapRequest;
import com.kapu.kapuproject.model.Status;
import com.kapu.kapuproject.model.User;
import com.kapu.kapuproject.service.NotificationService;
import com.kapu.kapuproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // 🔔 Main inbox : shows all
    @GetMapping
    public String showAllNotifications(Model model, Principal principal) {
        return loadNotificationsFor(model, principal, null, "All Notifications");
    }

    // 📨 Swap Requests
    @GetMapping("/swap-requests")
    public String showSwapRequests(Model model, Principal principal) {
        return loadNotificationsFor(model, principal, NotificationType.SWAP_REQUEST, "Swap Requests");
    }

    // ✅ Confirmations
    @GetMapping("/request-confirmations")
    public String showConfirmations(Model model, Principal principal) {
        return loadNotificationsFor(model, principal, NotificationType.SUCCESS, "Request Confirmations");
    }

    // 📩 From Reze
    @GetMapping("/from-reze")
    public String showFromReze(Model model, Principal principal) {
        return loadNotificationsFor(model, principal, NotificationType.FROM_REZE, "From Rezeption");
    }

    // 🧠 Reusable method
    private String loadNotificationsFor(Model model, Principal principal, NotificationType filter, String title) {
        if (principal != null) {
            String email = principal.getName();
            Optional<User> userOpt = userService.getUserByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<Notification> notifications = notificationService.getAllNotifications(user);

                // Filtro por tipo si se especifica
                if (filter != null) {
                    notifications = notifications.stream()
                            .filter(n -> n.getType() == filter)
                            .collect(Collectors.toList());
                    
                    // 👇 Filtro adicional para ocultar SWAP_REQUESTS aceptadas o rechazadas
                    if (filter == NotificationType.SWAP_REQUEST) {
                        notifications = notifications.stream()
                                .filter(n -> {
                                    ShiftSwapRequest swapRequest = n.getSwapRequest();
                                    return swapRequest != null && swapRequest.getStatus() == Status.PENDING;
                                })
                                .collect(Collectors.toList());
                    }
                }



                model.addAttribute("notifications", notifications);
                model.addAttribute("sectionTitle", title);

                // Marck as allready read
                notifications.stream()
                        .filter(n -> !n.isReadFlag())
                        .forEach(notificationService::markAsRead);
            }
        }

        return "notifications";
    }
}
