package com.app.app.repository;

import com.app.app.model.ActiveCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ActiveCallRepository extends JpaRepository<ActiveCall, String> {
    boolean existsByCallerEmailOrReceiverEmail(String callerEmail, String receiverEmail);
    Optional<ActiveCall> findByCallerEmailOrReceiverEmail(String callerEmail, String receiverEmail);
    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}