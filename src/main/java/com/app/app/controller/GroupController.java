package com.app.app.controller;

import com.app.app.dto.GroupActivityDTO;
import com.app.app.dto.GroupDTO;
import com.app.app.dto.GroupMemberDTO;
import com.app.app.model.*;
import com.app.app.repository.*;
import com.app.app.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final GroupCallSessionRepository sessionRepository;
    private final GroupCallParticipantRepository participantRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final GroupActivityRepository activityRepository;

    public GroupController(GroupRepository groupRepository,
                           GroupMembershipRepository membershipRepository,
                           UserRepository userRepository,
                           FileStorageService fileStorageService,
                           GroupCallSessionRepository sessionRepository,
                           GroupCallParticipantRepository participantRepository,
                           GroupMessageRepository groupMessageRepository, GroupActivityRepository activityRepository) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
        this.groupMessageRepository = groupMessageRepository;
        this.activityRepository = activityRepository;
    }

    public record CreateGroupRequest(@NotBlank String name, @NotEmpty Set<Long> memberIds) {}
    public record RenameGroupRequest(@NotBlank String name) {}
    public record AddMembersRequest(@NotEmpty Set<Long> memberIds) {}
    public record ChangeRoleRequest(@NotBlank String role) {}
    public record TransferOwnershipRequest(Long newOwnerUserId) {}

    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody CreateGroupRequest request, Authentication auth) {
        User creator = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Group group = new Group();
        group.setName(request.name());
        Group saved = groupRepository.save(group);

        addMembership(saved, creator, GroupRole.OWNER);
        logActivity(saved, GroupActivityType.CREATED, creator, null, request.name());

        for (User member : userRepository.findAllById(request.memberIds())) {
            if (member.getId().equals(creator.getId())) continue;
            addMembership(saved, member, GroupRole.MEMBER);
            logActivity(saved, GroupActivityType.MEMBER_ADDED, creator, member, null);
        }

        return ResponseEntity.ok(toDTO(saved));
    }

    @GetMapping
    public List<GroupDTO> getMyGroups(Authentication auth) {
        List<Long> groupIds = groupRepository.findGroupIdsByMemberEmail(auth.getName());
        if (groupIds.isEmpty()) return List.of();

        List<Group> groups = groupRepository.findAllById(groupIds);
        return groups.stream().map(this::toDTO).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable Long id, Authentication auth) {
        Group group = requireMember(id, auth.getName());
        return ResponseEntity.ok(toDTO(group));
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<GroupDTO> rename(@PathVariable Long id, @Valid @RequestBody RenameGroupRequest request, Authentication auth) {
        Group group = requireRole(id, auth.getName(), GroupRole.OWNER, GroupRole.ADMIN);
        User actor = userRepository.findByEmail(auth.getName()).orElseThrow();

        String oldName = group.getName();
        group.setName(request.name());
        logActivity(group, GroupActivityType.RENAMED, actor, null, oldName + " → " + request.name());

        return ResponseEntity.ok(toDTO(groupRepository.save(group)));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<GroupDTO> addMembers(@PathVariable Long id, @Valid @RequestBody AddMembersRequest request, Authentication auth) {
        Group group = requireRole(id, auth.getName(), GroupRole.OWNER, GroupRole.ADMIN);
        User actor = userRepository.findByEmail(auth.getName()).orElseThrow();

        Set<Long> existingIds = membershipRepository.findByGroupId(id).stream()
                .map(m -> m.getUser().getId()).collect(Collectors.toSet());

        for (User member : userRepository.findAllById(request.memberIds())) {
            if (existingIds.contains(member.getId())) continue;
            addMembership(group, member, GroupRole.MEMBER);
            logActivity(group, GroupActivityType.MEMBER_ADDED, actor, member, null);
        }

        return ResponseEntity.ok(toDTO(group));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<GroupDTO> removeMember(@PathVariable Long id, @PathVariable Long userId, Authentication auth) {
        Group group = requireRole(id, auth.getName(), GroupRole.OWNER, GroupRole.ADMIN);
        User actor = userRepository.findByEmail(auth.getName()).orElseThrow();

        GroupMembership target = membershipRepository.findByGroupId(id).stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        if (target.getRole() == GroupRole.OWNER) {
            throw new IllegalStateException("The owner cannot be removed — transfer ownership first");
        }

        User removedUser = target.getUser();
        membershipRepository.delete(target);
        logActivity(group, GroupActivityType.MEMBER_REMOVED, actor, removedUser, null);

        return ResponseEntity.ok(toDTO(group));
    }

    @PutMapping("/{id}/members/{userId}/role")
    public ResponseEntity<GroupDTO> changeRole(@PathVariable Long id, @PathVariable Long userId,
                                               @Valid @RequestBody ChangeRoleRequest request, Authentication auth) {
        Group group = requireRole(id, auth.getName(), GroupRole.OWNER);
        User actor = userRepository.findByEmail(auth.getName()).orElseThrow();

        GroupMembership target = membershipRepository.findByGroupId(id).stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        GroupRole newRole = GroupRole.valueOf(request.role());
        if (newRole == GroupRole.OWNER) {
            throw new IllegalArgumentException("Use the transfer-ownership endpoint to change the owner");
        }
        if (target.getRole() == GroupRole.OWNER) {
            throw new IllegalStateException("Cannot change the owner's role directly");
        }

        String oldRole = target.getRole().name();
        target.setRole(newRole);
        membershipRepository.save(target);
        logActivity(group, GroupActivityType.ROLE_CHANGED, actor, target.getUser(), oldRole + " → " + newRole);

        return ResponseEntity.ok(toDTO(group));
    }

    @PostMapping("/{id}/transfer-ownership")
    public ResponseEntity<GroupDTO> transferOwnership(@PathVariable Long id, @RequestBody TransferOwnershipRequest request, Authentication auth) {
        Group group = requireRole(id, auth.getName(), GroupRole.OWNER);

        GroupMembership currentOwner = membershipRepository.findByGroupIdAndUserEmail(id, auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Membership not found"));

        GroupMembership newOwner = membershipRepository.findByGroupId(id).stream()
                .filter(m -> m.getUser().getId().equals(request.newOwnerUserId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Target member not found in this group"));

        currentOwner.setRole(GroupRole.ADMIN);
        newOwner.setRole(GroupRole.OWNER);
        membershipRepository.save(currentOwner);
        membershipRepository.save(newOwner);

        logActivity(group, GroupActivityType.OWNERSHIP_TRANSFERRED, currentOwner.getUser(), newOwner.getUser(), null);

        return ResponseEntity.ok(toDTO(group));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leave(@PathVariable Long id, Authentication auth) {
        GroupMembership membership = membershipRepository.findByGroupIdAndUserEmail(id, auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Not a member of this group"));

        if (membership.getRole() == GroupRole.OWNER) {
            throw new IllegalStateException("The owner cannot leave — transfer ownership or delete the group instead");
        }

        logActivity(membership.getGroup(), GroupActivityType.MEMBER_LEFT, membership.getUser(), null, null);
        membershipRepository.delete(membership);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id, Authentication auth) {
        Group group = requireRole(id, auth.getName(), GroupRole.OWNER);

        List<GroupCallSession> sessions = sessionRepository.findByGroupId(id);
        for (GroupCallSession session : sessions) {
            participantRepository.deleteAll(participantRepository.findByCallId(session.getCallId()));
        }
        sessionRepository.deleteAll(sessions);

        groupMessageRepository.deleteAll(groupMessageRepository.findByGroupId(id));
        activityRepository.deleteByGroupId(id);
        membershipRepository.deleteAll(membershipRepository.findByGroupId(id));

        groupRepository.delete(group);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<GroupDTO> uploadAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication auth) throws IOException {
        Group group = requireRole(id, auth.getName(), GroupRole.OWNER, GroupRole.ADMIN);
        User actor = userRepository.findByEmail(auth.getName()).orElseThrow();

        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        String url = fileStorageService.upload(file, "group-avatars");
        group.setAvatarUrl(url);
        logActivity(group, GroupActivityType.AVATAR_CHANGED, actor, null, null);

        return ResponseEntity.ok(toDTO(groupRepository.save(group)));
    }

    // ── Helpers ──────────────────────────────────────────

    private void addMembership(Group group, User user, GroupRole role) {
        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setRole(role);
        membershipRepository.save(membership);
    }

    /** No longer touches group.getMemberships() at all — plain findById is enough now. */
    private Group requireMember(Long groupId, String email) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        boolean isMember = membershipRepository.findByGroupId(groupId).stream()
                .anyMatch(m -> m.getUser().getEmail().equals(email));
        if (!isMember) throw new AccessDeniedException("Not a member of this group");
        return group;
    }

    private Group requireRole(Long groupId, String email, GroupRole... allowed) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        GroupRole actual = membershipRepository.findByGroupId(groupId).stream()
                .filter(m -> m.getUser().getEmail().equals(email))
                .map(GroupMembership::getRole)
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Not a member of this group"));

        boolean allowedRole = List.of(allowed).contains(actual);
        if (!allowedRole) throw new AccessDeniedException("Insufficient permissions for this action");

        return group;
    }

    /** Builds the DTO from GroupMembershipRepository directly — bypasses
     *  Group.memberships collection-fetching entirely. */
    private GroupDTO toDTO(Group group) {
        List<GroupMembership> memberships = membershipRepository.findByGroupIdWithUser(group.getId());

        Set<GroupMemberDTO> members = memberships.stream()
                .map(m -> new GroupMemberDTO(
                        m.getUser().getId(), m.getUser().getEmail(), m.getUser().getName(),
                        m.getUser().getPictureUrl(), m.getRole().name()))
                .collect(Collectors.toSet());

        return new GroupDTO(group.getId(), group.getName(), group.getAvatarUrl(), members);
    }

    private void logActivity(Group group, GroupActivityType type, User actor, User target, String detail) {
        GroupActivity activity = new GroupActivity();
        activity.setGroup(group);
        activity.setType(type);
        activity.setActor(actor);
        activity.setTarget(target);
        activity.setDetail(detail);
        activityRepository.save(activity);
    }

    @GetMapping("/{id}/activity")
    public List<GroupActivityDTO> getActivity(@PathVariable Long id, Authentication auth) {
        requireMember(id, auth.getName());
        return activityRepository.findByGroupId(id).stream()
                .map(a -> new GroupActivityDTO(
                        a.getId(),
                        a.getType().name(),
                        a.getActor() != null ? a.getActor().getName() : "System",
                        a.getTarget() != null ? a.getTarget().getName() : null,
                        a.getDetail(),
                        a.getTimestamp()
                ))
                .toList();
    }
}