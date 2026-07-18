// GroupRepository.java
package com.app.app.repository;

import com.app.app.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT DISTINCT g.id FROM Group g JOIN g.memberships m WHERE m.user.email = :email")
    List<Long> findGroupIdsByMemberEmail(@Param("email") String email);

    // Single-level fetch only — memberships, no nested user fetch here.
    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.memberships WHERE g.id IN :ids")
    List<Group> findAllByIdsWithMembers(@Param("ids") List<Long> ids);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.memberships WHERE g.id = :id")
    Optional<Group> findByIdWithMembers(@Param("id") Long id);
}