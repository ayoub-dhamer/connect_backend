// GroupMessageRepository.java
package com.app.app.repository;

import com.app.app.model.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    @Query("SELECT m FROM GroupMessage m WHERE m.group.id = :groupId ORDER BY m.timestamp ASC")
    List<GroupMessage> findByGroupId(@Param("groupId") Long groupId);
}