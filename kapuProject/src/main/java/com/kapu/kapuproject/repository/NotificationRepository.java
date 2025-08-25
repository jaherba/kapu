package com.kapu.kapuproject.repository;

import com.kapu.kapuproject.model.Notification;
import com.kapu.kapuproject.model.NotificationType;
import com.kapu.kapuproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndReadFlagFalseOrderByCreatedAtDesc(User recipient);
    
    List<Notification> findByRecipientAndTypeOrderByCreatedAtDesc(User recipient, NotificationType type);

}
