// controller/CallLogController.java
package com.app.app.controller;

import com.app.app.dto.CallLogDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.model.CallLog;
import com.app.app.model.CallStatus;
import com.app.app.model.CallType;
import com.app.app.model.User;
import com.app.app.repository.CallLogRepository;
import com.app.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/calls")
public class CallLogController {

    private final CallLogRepository callLogRepository;
    private final UserRepository userRepository;
    private final CentralMapper centralMapper;

    public CallLogController(CallLogRepository callLogRepository,
                             UserRepository userRepository,
                             CentralMapper centralMapper) {
        this.callLogRepository = callLogRepository;
        this.userRepository = userRepository;
        this.centralMapper = centralMapper;
    }

    public record LogCallRequest(
            String callId,
            String callerEmail,
            String receiverEmail,
            String callType,
            String status,
            String startedAt,
            String endedAt,
            Integer durationSeconds
    ) {}

    @PostMapping
    public ResponseEntity<Void> logCall(@RequestBody LogCallRequest request) {
        User caller = userRepository.findByEmail(request.callerEmail())
                .orElseThrow(() -> new EntityNotFoundException("Caller not found"));
        User receiver = userRepository.findByEmail(request.receiverEmail())
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

        CallLog log = callLogRepository.findById(request.callId()).orElseGet(() -> {
            CallLog newLog = new CallLog();
            newLog.setId(request.callId());
            return newLog;
        });

        log.setCaller(caller);
        log.setReceiver(receiver);
        log.setCallType(CallType.valueOf(request.callType()));
        log.setStatus(CallStatus.valueOf(request.status()));
        log.setStartedAt(parseTimestamp(request.startedAt()));
        if (request.endedAt() != null) {
            log.setEndedAt(parseTimestamp(request.endedAt()));
        }
        log.setDurationSeconds(request.durationSeconds());

        try {
            callLogRepository.save(log);
        } catch (DataIntegrityViolationException e) {
            // Lost the race to a concurrent request for the same callId — the row
            // now exists; re-fetch and update it instead of failing the whole request.
            CallLog existing = callLogRepository.findById(request.callId())
                    .orElseThrow(() -> e);
            existing.setCaller(caller);
            existing.setReceiver(receiver);
            existing.setCallType(CallType.valueOf(request.callType()));
            existing.setStatus(CallStatus.valueOf(request.status()));
            existing.setStartedAt(parseTimestamp(request.startedAt()));
            if (request.endedAt() != null) {
                existing.setEndedAt(parseTimestamp(request.endedAt()));
            }
            existing.setDurationSeconds(request.durationSeconds());
            callLogRepository.save(existing);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/history/{contactEmail}")
    public List<CallLogDTO> getHistory(@PathVariable String contactEmail, Authentication auth) {
        return callLogRepository.findConversation(auth.getName(), contactEmail)
                .stream()
                .map(centralMapper::toDTO)
                .toList();
    }

    private LocalDateTime parseTimestamp(String isoString) {
        return LocalDateTime.ofInstant(Instant.parse(isoString), ZoneId.systemDefault());
    }
}