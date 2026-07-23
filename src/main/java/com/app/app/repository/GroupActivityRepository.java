package com.app.app.repository;

import com.app.app.model.GroupActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;


@Repository
public interface GroupActivityRepository extends JpaRepository<GroupActivity, Long> {

    @Query("SELECT a FROM GroupActivity a JOIN FETCH a.group WHERE a.group.id = :groupId ORDER BY a.timestamp ASC")
    List<GroupActivity> findByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Query("DELETE FROM GroupActivity a WHERE a.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);}