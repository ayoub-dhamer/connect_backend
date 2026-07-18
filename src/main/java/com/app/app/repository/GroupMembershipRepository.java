package com.app.app.repository;

import com.app.app.model.GroupMembership;
import com.app.app.model.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    Optional<GroupMembership> findByGroupIdAndUserEmail(Long groupId, String email);

    List<GroupMembership> findByGroupId(Long groupId);

    @Query("SELECT m FROM GroupMembership m WHERE m.group.id = :groupId AND m.role = :role")
    List<GroupMembership> findByGroupIdAndRole(@Param("groupId") Long groupId, @Param("role") GroupRole role);
}