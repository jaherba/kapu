package com.kapu.kapuproject.repository;

import com.kapu.kapuproject.model.ShiftSwapRequest;
import com.kapu.kapuproject.model.User;
import com.kapu.kapuproject.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftSwapRequestRepository extends JpaRepository<ShiftSwapRequest, Long> {
    List<ShiftSwapRequest> findByRecipient(User recipient);
    List<ShiftSwapRequest> findByRequester(User requester);
    List<ShiftSwapRequest> findByStatus(Status status);
}
