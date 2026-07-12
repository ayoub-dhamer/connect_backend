package com.app.app.repository;

import com.app.app.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.sender.email = :userA AND m.receiver.email = :userB)
           OR (m.sender.email = :userB AND m.receiver.email = :userA)
        ORDER BY m.timestamp ASC
        """)
    List<ChatMessage> findConversation(@Param("userA") String userA, @Param("userB") String userB);
}
