package com.kapu.kapuproject.service;

import com.kapu.kapuproject.model.ShiftSwapRequest;
import com.kapu.kapuproject.model.Status;
import com.kapu.kapuproject.model.User;
import com.kapu.kapuproject.repository.ShiftSwapRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShiftSwapRequestService {

    private final ShiftSwapRequestRepository repository;

    @Autowired
    public ShiftSwapRequestService(ShiftSwapRequestRepository repository) {
        this.repository = repository;
    }

    public ShiftSwapRequest saveRequest(ShiftSwapRequest request) {
        return repository.save(request);
    }

    public Optional<ShiftSwapRequest> getById(Long id) {
        return repository.findById(id);
    }

    public List<ShiftSwapRequest> getRequestsByRecipient(User user) {
        return repository.findByRecipient(user);
    }

    public List<ShiftSwapRequest> getRequestsByRequester(User user) {
        return repository.findByRequester(user);
    }

    public List<ShiftSwapRequest> getRequestsByStatus(Status status) {
        return repository.findByStatus(status);
    }

    public void deleteRequest(Long id) {
        repository.deleteById(id);
    }
}
