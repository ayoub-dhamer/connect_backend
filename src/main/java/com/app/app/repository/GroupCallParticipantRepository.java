// GroupCallParticipantRepository.java
package com.app.app.repository;

import com.app.app.model.GroupCallParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupCallParticipantRepository extends JpaRepository<GroupCallParticipant, Long> {

    @Query("SELECT p FROM GroupCallParticipant p WHERE p.call.callId = :callId")
    List<GroupCallParticipant> findByCallId(@Param("callId") String callId);

    @Query("SELECT p FROM GroupCallParticipant p WHERE p.call.callId = :callId AND p.user.email = :email")
    Optional<GroupCallParticipant> findByCallIdAndUserEmail(@Param("callId") String callId, @Param("email") String email);
}