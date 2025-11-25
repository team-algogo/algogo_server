package com.ssafy.algogo.program.group.controller;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import com.ssafy.algogo.program.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {

  private final GroupService groupService;


//  @GetMapping("/{programId}")
//  public SuccessResponse getGroupRoomDetail(
//      @PathVariable Long programId
//  ){
//
//    GroupRoomResponseDto groupRoomDetail = groupService.getGroupRoomDetail(programId);
//
//    return new SuccessResponse("그룹방 상세 정보 조회 성공", groupRoomDetail);
//  }

  @PostMapping
  public SuccessResponse createGroupRoom(
      //@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody @Valid CreateGroupRoomRequestDto createGroupRoomRequestDto
  ){
    GroupRoomResponseDto groupRoomResponseDto = groupService.createGroupRoom(1L, createGroupRoomRequestDto);

    return new SuccessResponse("그룹방 생성 성공", groupRoomResponseDto);
  }
}