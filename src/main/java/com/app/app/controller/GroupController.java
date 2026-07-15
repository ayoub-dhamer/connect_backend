package com.app.app.controller;

import com.app.app.dto.GroupDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.model.Group;
import com.app.app.model.User;
import com.app.app.repository.GroupRepository;
import com.app.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CentralMapper centralMapper;

    public GroupController(GroupRepository groupRepository, UserRepository userRepository, CentralMapper centralMapper) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.centralMapper = centralMapper;
    }

    public record CreateGroupRequest(
            @NotBlank String name,
            @NotEmpty Set<Long> memberIds
    ) {}

    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody CreateGroupRequest request, Authentication auth) {
        User creator = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Set<User> members = new HashSet<>(userRepository.findAllById(request.memberIds()));
        members.add(creator); // creator is always a member

        Group group = new Group();
        group.setName(request.name());
        group.setCreatedBy(creator);
        group.setMembers(members);

        Group saved = groupRepository.save(group);
        return ResponseEntity.ok(centralMapper.toDTO(saved));
    }

    @GetMapping
    public List<GroupDTO> getMyGroups(Authentication auth) {
        return groupRepository.findAllByMemberEmail(auth.getName())
                .stream()
                .map(centralMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable Long id, Authentication auth) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        boolean isMember = group.getMembers().stream().anyMatch(m -> m.getEmail().equals(auth.getName()));
        if (!isMember) throw new AccessDeniedException("Not a member of this group");

        return ResponseEntity.ok(centralMapper.toDTO(group));
    }
}