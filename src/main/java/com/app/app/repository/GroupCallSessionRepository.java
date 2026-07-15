// GroupCallSessionRepository.java
package com.app.app.repository;

import com.app.app.model.GroupCallSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupCallSessionRepository extends JpaRepository<GroupCallSession, String> {

    @Query("SELECT s FROM GroupCallSession s WHERE s.group.id = :groupId ORDER BY s.startedAt ASC")
    List<GroupCallSession> findByGroupId(@Param("groupId") Long groupId);
}