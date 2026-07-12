package com.app.app.service;

import com.app.app.model.ActiveCall;
import com.app.app.repository.ActiveCallRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CallStateService {

    private final ActiveCallRepository activeCallRepository;

    public CallStateService(ActiveCallRepository activeCallRepository) {
        this.activeCallRepository = activeCallRepository;
    }

    public boolean isBusy(String email) {
        return activeCallRepository.existsByCallerEmailOrReceiverEmail(email, email);
    }

    @Transactional
    public boolean registerRinging(String callId, String callerEmail, String receiverEmail) {
        purgeStale();
        if (isBusy(callerEmail) || isBusy(receiverEmail)) {
            return false;
        }

        ActiveCall call = new ActiveCall();
        call.setCallId(callId);
        call.setCallerEmail(callerEmail);
        call.setReceiverEmail(receiverEmail);

        try {
            activeCallRepository.save(call);
            return true;
        } catch (DataIntegrityViolationException e) {
            // Lost a race to a concurrent request for the same pair — treat as busy.
            return false;
        }
    }

    private void purgeStale() {
        // Anything older than 4 hours is almost certainly an orphaned row from a
        // crash/restart rather than a real ongoing call — clean it up defensively.
        activeCallRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusHours(4));
    }

    @Transactional
    public void clear(String callId) {
        activeCallRepository.deleteById(callId);
    }
}