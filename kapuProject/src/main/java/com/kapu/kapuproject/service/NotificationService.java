package com.kapu.kapuproject.service;

import com.kapu.kapuproject.model.Notification;
import com.kapu.kapuproject.model.NotificationType;
import com.kapu.kapuproject.model.User;
import com.kapu.kapuproject.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kapu.kapuproject.model.ShiftSwapRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(User recipient, String message, NotificationType type, String contextLink) {
    	
    	 System.out.println("💾 Saving notification to DB: " + type + " → " + recipient.getName());
    	
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadFlag(false);
        notification.setContextLink(contextLink);

        notificationRepository.save(notification);
    }
    
    public void createNotification(User recipient, String message, NotificationType type, String contextLink, ShiftSwapRequest swapRequest) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setContextLink(contextLink);
        notification.setSwapRequest(swapRequest);

        notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByRecipientAndReadFlagFalseOrderByCreatedAtDesc(user);
    }

    public void markAsRead(Notification notification) {
        notification.setReadFlag(true);
        notificationRepository.save(notification);
    }
    
    public List<Notification> getNotificationsByUserAndType(User user, NotificationType type) {
        return notificationRepository.findByRecipientAndTypeOrderByCreatedAtDesc(user, type);
    }

}
