package com.ssafy.algogo.program.group.controller;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.program.dto.request.ApplyProgramInviteRequestDto;
import com.ssafy.algogo.program.dto.response.GetGroupJoinStateListResponseDto;
import com.ssafy.algogo.program.group.config.GroupAuthorize;
import com.ssafy.algogo.program.group.config.GroupId;
import com.ssafy.algogo.program.group.dto.request.CheckGroupNameRequestDto;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupInviteStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupJoinStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.CheckGroupNameResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import com.ssafy.algogo.program.group.entity.GroupRole;
import com.ssafy.algogo.program.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {

  private final GroupService groupService;

  @GetMapping("/{programId}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponse getGroupRoomDetail(
      @PathVariable Long programId
  ){

    GroupRoomResponseDto groupRoomResponseDto = groupService.getGroupRoomDetail(programId);

    return new SuccessResponse("그룹방 상세 정보 조회 성공", groupRoomResponseDto);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponse createGroupRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody @Valid CreateGroupRoomRequestDto createGroupRoomRequestDto
  ){
    GroupRoomResponseDto groupRoomResponseDto = groupService.createGroupRoom(customUserDetails.getUserId(), createGroupRoomRequestDto);

    return new SuccessResponse("그룹방 생성 성공", groupRoomResponseDto);
  }

  @PostMapping("/check/groupnames")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponse checkGroupName(
      @RequestBody @Valid CheckGroupNameRequestDto checkGroupNameRequestDto
  ){
    CheckGroupNameResponseDto checkGroupNameResponseDto = groupService.checkGroupName(checkGroupNameRequestDto);

    String message = null;
    if(checkGroupNameResponseDto.isAvailable()){
      message = "사용가능한 그룹명입니다.";
    }else{
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
  ){
    GroupRoomResponseDto groupRoomResponseDto = groupService.updateGroupRoom(programId, updateGroupRoomRequestDto);

    return new SuccessResponse("그룹방 수정 성공", groupRoomResponseDto);
  }

  @PostMapping("/{programId}/join")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponse applyGroupJoin(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long programId
  ){
    groupService.applyGroupJoin(customUserDetails.getUserId(), programId);

    return new SuccessResponse("그룹 참여 신청 성공", null);
  }

  @PutMapping("/{programId}/join/{joinId}")
  @ResponseStatus(HttpStatus.OK)
  @GroupAuthorize(minRole = GroupRole.ADMIN)
  public SuccessResponse updateGroupJoinState(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable @GroupId Long programId,
      @PathVariable Long joinId,
      @RequestBody @Valid UpdateGroupJoinStateRequestDto updateGroupJoinStateRequestDto
  ){
    groupService.updateGroupJoinState(customUserDetails.getUserId(), programId, joinId,
        updateGroupJoinStateRequestDto);

    return new SuccessResponse("그룹 회원 참여 신청 상태 수정 성공", null);
  }

  @GetMapping("/{programId}/join/lists")
  @ResponseStatus(HttpStatus.OK)
  @GroupAuthorize(minRole = GroupRole.ADMIN)
  public SuccessResponse getGroupJoinState(
      @PathVariable @GroupId Long programId
  ){
    GetGroupJoinStateListResponseDto getGroupJoinStateListResponseDto = groupService.getGroupJoinState(programId);

    return new SuccessResponse("그룹 회원 참여신청 리스트 조회에 성공했습니다.", getGroupJoinStateListResponseDto);
  }

  @PostMapping("/{programId}/invite")
  @ResponseStatus(HttpStatus.OK)
  @GroupAuthorize(minRole = GroupRole.ADMIN)
  public SuccessResponse applyGroupInvite(
      @PathVariable @GroupId Long programId,
      @RequestBody @Valid ApplyProgramInviteRequestDto applyProgramInviteRequestDto
  ){
    groupService.applyGroupInvite(programId, applyProgramInviteRequestDto);

    return new SuccessResponse("그룹 회원 초대 성공", null);
  }

  @PutMapping("/{programId}/invite/{inviteId}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponse updateGroupInviteState(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long programId,
      @PathVariable Long inviteId,
      @RequestBody @Valid UpdateGroupInviteStateRequestDto updateGroupInviteStateRequestDto
  ){
    groupService.updateGroupInviteState(customUserDetails.getUserId(), programId, inviteId,
        updateGroupInviteStateRequestDto);

    return new SuccessResponse(" 그룹회원 초대 상태 수정에 성공했습니다.", null);
  }

}