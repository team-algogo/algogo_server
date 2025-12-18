package com.ssafy.algogo.program.group.controller;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.problem.dto.request.ProgramProblemCreateRequestDto;
import com.ssafy.algogo.problem.dto.request.ProgramProblemDeleteRequestDto;
import com.ssafy.algogo.problem.dto.response.ProgramProblemPageResponseDto;
import com.ssafy.algogo.program.dto.request.ApplyProgramInviteRequestDto;
import com.ssafy.algogo.program.dto.response.GetProgramInviteStateListResponseDto;
import com.ssafy.algogo.program.dto.response.GetProgramJoinStateListResponseDto;
import com.ssafy.algogo.program.group.config.GroupAuthorize;
import com.ssafy.algogo.program.group.config.GroupId;
import com.ssafy.algogo.program.group.dto.request.CheckGroupNameRequestDto;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupInviteStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupJoinStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupMemberRoleRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.CheckGroupNameResponseDto;
import com.ssafy.algogo.program.group.dto.response.GetGroupMemberListResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomPageResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import com.ssafy.algogo.program.group.dto.response.MyGroupRoomPageResponseDto;
import com.ssafy.algogo.program.group.entity.GroupRole;
import com.ssafy.algogo.program.group.service.GroupService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/lists")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse getGroupRoomList(
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
        @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection,
        @RequestParam(value = "size", defaultValue = "10") Integer size,
        @RequestParam(value = "page", defaultValue = "0") Integer page,
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        Long userId = (customUserDetails != null) ? customUserDetails.getUserId() : null;

        GroupRoomPageResponseDto groupRoomPageResponseDto =
            groupService.getGroupRoomList(keyword, pageable, userId);

        return new SuccessResponse("그룹방 리스트 조회 성공", groupRoomPageResponseDto);
    }

    @GetMapping("/{programId}")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse getGroupRoomDetail(
        @PathVariable Long programId,
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Long userId = (customUserDetails != null) ? customUserDetails.getUserId() : null;

        GroupRoomResponseDto groupRoomResponseDto = groupService.getGroupRoomDetail(programId,
            userId);

        return new SuccessResponse("그룹방 상세 정보 조회 성공", groupRoomResponseDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessResponse createGroupRoom(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestBody @Valid CreateGroupRoomRequestDto createGroupRoomRequestDto
    ) {
        GroupRoomResponseDto groupRoomResponseDto = groupService.createGroupRoom(
            customUserDetails.getUserId(), createGroupRoomRequestDto);

        return new SuccessResponse("그룹방 생성 성공", groupRoomResponseDto);
    }

    @PostMapping("/check/groupnames")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse checkGroupName(
        @RequestBody @Valid CheckGroupNameRequestDto checkGroupNameRequestDto
    ) {
        CheckGroupNameResponseDto checkGroupNameResponseDto = groupService.checkGroupName(
            checkGroupNameRequestDto);

        String message = null;
        if (checkGroupNameResponseDto.isAvailable()) {
            message = "사용가능한 그룹명입니다.";
        } else {
            message = "이미 존재하는 그룹명입니다.";
        }
        return new SuccessResponse(message, checkGroupNameResponseDto);
    }

    @PutMapping("/{programId}")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.MANAGER)
    public SuccessResponse updateGroupRoom(
        @PathVariable @GroupId Long programId,
        @RequestBody UpdateGroupRoomRequestDto updateGroupRoomRequestDto
    ) {
        GroupRoomResponseDto groupRoomResponseDto = groupService.updateGroupRoom(programId,
            updateGroupRoomRequestDto);

        return new SuccessResponse("그룹방 수정 성공", groupRoomResponseDto);
    }

    @DeleteMapping("/{programId}")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.MANAGER)
    public SuccessResponse deleteGroupRoom( // cascade 문제없는 지 검증
        @PathVariable @GroupId Long programId
    ) {
        groupService.deleteGroupRoom(programId);

        return new SuccessResponse("그룹방 삭제를 성공했습니다.", null);
    }

    @PostMapping("/{programId}/join")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse applyGroupJoin( // 검증
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long programId
    ) {
        groupService.applyGroupJoin(customUserDetails.getUserId(), programId);

        return new SuccessResponse("그룹 참여 신청 성공", null);
    }

    @PutMapping("/{programId}/join/{joinId}")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.ADMIN)
    public SuccessResponse updateGroupJoinState( // 검증
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable @GroupId Long programId,
        @PathVariable Long joinId,
        @RequestBody @Valid UpdateGroupJoinStateRequestDto updateGroupJoinStateRequestDto
    ) {
        groupService.updateGroupJoinState(customUserDetails.getUserId(), programId, joinId,
            updateGroupJoinStateRequestDto);

        return new SuccessResponse("그룹 회원 참여 신청 상태 수정 성공", null);
    }

    @GetMapping("/{programId}/join/lists")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.ADMIN)
    public SuccessResponse getGroupJoinState( // 검증
        @PathVariable @GroupId Long programId
    ) {
        GetProgramJoinStateListResponseDto getProgramJoinStateListResponseDto = groupService.getGroupJoinState(
            programId);

        return new SuccessResponse("그룹 회원 참여신청 리스트 조회에 성공했습니다.",
            getProgramJoinStateListResponseDto);
    }

    @PostMapping("/{programId}/invite")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.ADMIN)
    public SuccessResponse applyGroupInvite( // 검증
        @PathVariable @GroupId Long programId,
        @RequestBody @Valid ApplyProgramInviteRequestDto applyProgramInviteRequestDto
    ) {
        groupService.applyGroupInvite(programId, applyProgramInviteRequestDto);

        return new SuccessResponse("그룹 회원 초대 성공", null);
    }

    @PutMapping("/{programId}/invite/{inviteId}")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse updateGroupInviteState( // 검증
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long programId,
        @PathVariable Long inviteId,
        @RequestBody @Valid UpdateGroupInviteStateRequestDto updateGroupInviteStateRequestDto
    ) {
        groupService.updateGroupInviteState(customUserDetails.getUserId(), programId, inviteId,
            updateGroupInviteStateRequestDto);

        return new SuccessResponse(" 그룹회원 초대 상태 수정에 성공했습니다.", null);
    }

    @DeleteMapping("/{programId}/invite/{inviteId}")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.ADMIN)
    public SuccessResponse deleteGroupInvite( // 검증
        @PathVariable @GroupId Long programId,
        @PathVariable Long inviteId
    ) {
        groupService.deleteGroupInvite(programId, inviteId);

        return new SuccessResponse(" 그룹회원 초대 취소를 성공했습니다.", null);
    }

    @GetMapping("/{programId}/invite/lists")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.ADMIN)
    public SuccessResponse getGroupInviteState( // 검증
        @PathVariable @GroupId Long programId
    ) {
        GetProgramInviteStateListResponseDto getProgramInviteStateListResponseDto = groupService.getGroupInviteState(
            programId);

        return new SuccessResponse("그룹 회원 초대 리스트 조회에 성공했습니다.",
            getProgramInviteStateListResponseDto);
    }

    @GetMapping("/{programId}/users")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.USER)
    public SuccessResponse getGroupMember( // 검증
        @PathVariable @GroupId Long programId
    ) {
        GetGroupMemberListResponseDto getGroupMemberListResponseDto = groupService.getGroupMember(
            programId);

        return new SuccessResponse("그룹 회원 조회 성공", getGroupMemberListResponseDto);
    }

    @PutMapping("/{programId}/users/{programUserId}/role")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.ADMIN)
    public SuccessResponse updateGroupMemberRole( // 검증
        @PathVariable @GroupId Long programId,
        @PathVariable Long programUserId,
        @RequestBody @Valid UpdateGroupMemberRoleRequestDto updateGroupMemberRoleRequestDto
    ) {
        groupService.updateGroupMemberRole(programId, programUserId,
            updateGroupMemberRoleRequestDto);

        return new SuccessResponse("그룹회원 권한 수정을 성공했습니다.", null);
    }

    @PutMapping("/{programId}/users/{programUserId}")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.USER)
    public SuccessResponse deleteGroupMember(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable @GroupId Long programId,
        @PathVariable Long programUserId
    ) {
        groupService.deleteGroupMember(customUserDetails.getUserId(), programId, programUserId);

        return new SuccessResponse("그룹회원 삭제를 성공했습니다.", null);
    }

    @PostMapping("/{programId}/problems")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.MANAGER)
    public SuccessResponse addGroupProblem(
        @PathVariable @GroupId Long programId,
        @RequestBody @Valid ProgramProblemCreateRequestDto programProblemCreateRequestDto
    ) {
        groupService.addGroupProblem(programId, programProblemCreateRequestDto);

        return new SuccessResponse("그룹 요소 추가(문제)를 성공했습니다.", null);
    }

    @GetMapping("/{programId}/problems/lists")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.USER)
    public SuccessResponse getGroupProblems(
        @PathVariable @GroupId Long programId,
        @RequestParam(value = "sortBy", defaultValue = "endDate") String sortBy,
        @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection,
        @RequestParam(value = "size", defaultValue = "10") Integer size,
        @RequestParam(value = "page", defaultValue = "0") Integer page
    ) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        ProgramProblemPageResponseDto response = groupService.getAllGroupProblems(programId,
            pageable);
        return new SuccessResponse("그룹 문제 리스트 조회 성공", response);
    }

    @DeleteMapping("/{programId}/problems")
    @ResponseStatus(HttpStatus.OK)
    @GroupAuthorize(minRole = GroupRole.MANAGER)
    public SuccessResponse deleteGroupProblems(
        @PathVariable @GroupId Long programId,
        @RequestBody @Valid ProgramProblemDeleteRequestDto programProblemDeleteRequestDto
    ) {
        groupService.deleteGroupProblems(programId, programProblemDeleteRequestDto);
        return new SuccessResponse("그룹문제 삭제를 성공했습니다.", null);
    }

    @GetMapping("/lists/me")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse getMyGroupRooms(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
        @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection,
        @RequestParam(value = "size", defaultValue = "10") Integer size,
        @RequestParam(value = "page", defaultValue = "0") Integer page
    ) {

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        MyGroupRoomPageResponseDto response =
            groupService.getMyGroupRooms(customUserDetails.getUserId(), pageable);

        return new SuccessResponse("내 그룹 조회 성공", response);
    }


}