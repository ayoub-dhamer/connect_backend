// repository/CallLogRepository.java
package com.app.app.repository;

import com.app.app.model.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, String> {

    @Query("""
        SELECT c FROM CallLog c
        WHERE (c.caller.email = :userA AND c.receiver.email = :userB)
           OR (c.caller.email = :userB AND c.receiver.email = :userA)
        ORDER BY c.startedAt ASC
        """)
    List<CallLog> findConversation(@Param("userA") String userA, @Param("userB") String userB);
}