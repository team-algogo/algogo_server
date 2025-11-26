package com.ssafy.algogo.program.group.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.program.entity.JoinStatus;
import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.entity.ProgramJoin;
import com.ssafy.algogo.program.entity.ProgramType;
import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.program.group.dto.request.CheckGroupNameRequestDto;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.dto.request.UpdateProgramJoinStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.CheckGroupNameResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import com.ssafy.algogo.program.group.entity.GroupRole;
import com.ssafy.algogo.program.group.entity.GroupRoom;
import com.ssafy.algogo.program.group.entity.GroupsUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.program.group.repository.GroupRepository;
import com.ssafy.algogo.program.group.repository.GroupUserRepository;
import com.ssafy.algogo.program.group.service.GroupService;
import com.ssafy.algogo.program.repository.ProgramJoinRepository;
import com.ssafy.algogo.program.repository.ProgramRepository;
import com.ssafy.algogo.program.repository.ProgramTypeRepository;
import com.ssafy.algogo.program.repository.ProgramUserRepository;
import com.ssafy.algogo.program.service.ProgramService;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

  private final ProgramService programService;

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final ProgramTypeRepository programTypeRepository;
  private final GroupUserRepository groupUserRepository;
  private final ProgramRepository programRepository;
  private final ProgramJoinRepository programJoinRepository;
  private final ProgramUserRepository programUserRepository;

  @Override
  @Transactional(readOnly = true)
  public GroupRoomResponseDto getGroupRoomDetail(Long programId) {
    GroupRoomResponseDto groupRoomResponseDto = groupRepository.getGroupRoomDetail(programId);

    if (groupRoomResponseDto == null) {
      throw new CustomException("해당 그룹방이 존재하지 않습니다.", ErrorCode.GROUP_NOT_FOUND);
    }

    return groupRoomResponseDto;
  }

  @Override
  public GroupRoomResponseDto createGroupRoom(Long userId, CreateGroupRoomRequestDto createGroupRoomRequestDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException("userId에 해당하는 데이터가 DB에 없습니다.",
            ErrorCode.USER_NOT_FOUND));

  ProgramType programType = programTypeRepository.findByName("group")
      .orElseThrow(() -> new CustomException("group에 해당하는 데이터가 DB에 없습니다.", ErrorCode.PROGRAM_TYPE_NOT_FOUND));

    boolean isTitleConflict = programRepository.existsByTitle(createGroupRoomRequestDto.getTitle());
    if (isTitleConflict) {
      throw new CustomException("이미 존재하는 그룹명이 있습니다.", ErrorCode.DUPLICATE_RESOURCE);
    }

    GroupRoom groupRoom = GroupRoom.create(
        createGroupRoomRequestDto.getTitle(),
        createGroupRoomRequestDto.getDescription(),
        programType,
        createGroupRoomRequestDto.getCapacity()
    );
    groupRepository.save(groupRoom);

    GroupsUser groupsUser = GroupsUser.create(
        ProgramUserStatus.ACTIVE,
        groupRoom,
        user,
        GroupRole.ADMIN
    );
    groupUserRepository.save(groupsUser);

    return groupRepository.getGroupRoomDetail(groupRoom.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public CheckGroupNameResponseDto checkGroupName(CheckGroupNameRequestDto checkGroupNameRequestDto) {
    return new CheckGroupNameResponseDto(!programRepository.existsByTitle(checkGroupNameRequestDto.getGroupTitle()));
  }

  @Override
  public GroupRoomResponseDto updateGroupRoom(Long programId, UpdateGroupRoomRequestDto updateGroupRoomRequestDto) {
    GroupRoom groupRoom = groupRepository.findById(programId)
        .orElseThrow(() -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

    if (updateGroupRoomRequestDto.getTitle() != null) {
      boolean isTitleConflict = programRepository.existsByTitle(updateGroupRoomRequestDto.getTitle());
      if (isTitleConflict) {
        throw new CustomException("이미 존재하는 그룹명이 있습니다.", ErrorCode.DUPLICATE_RESOURCE);
      }
    }

    groupRoom.updateGroupRoom(
        groupRoom.getTitle(),
        groupRoom.getDescription(),
        updateGroupRoomRequestDto.getCapacity()
    );

    groupRepository.save(groupRoom);

    return groupRepository.getGroupRoomDetail(groupRoom.getId());
  }

  @Override
  public void applyGroupJoin(Long userId, Long programId) {
    GroupRoom groupRoom = groupRepository.findById(programId)
        .orElseThrow(() -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

    programService.applyProgramJoin(userId, programId);

    // 방장에게 알람 보내는 로직 나중에 추가
  }

  @Override
  public void updateGroupJoinState(Long userId, Long programId, Long joinId,
      UpdateProgramJoinStateRequestDto updateProgramJoinStateRequestDto) {

    GroupRoom groupRoom = groupRepository.findById(programId)
        .orElseThrow(() -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

    // 해당 신청(joinId)이 실제 존재하는지 확인
    ProgramJoin programJoin = programJoinRepository.findById(joinId)
        .orElseThrow(() ->
            new CustomException("해당 참여 신청 정보를 찾을 수 없습니다.", ErrorCode.PROGRAM_JOIN_NOT_FOUND));

    // 신청이 해당 programId에 속해야 함
    if (!programJoin.getProgram().getId().equals(programId)) {
      throw new CustomException("해당 프로그램의 신청이 아닙니다.", ErrorCode.INVALID_PARAMETER);
    }

    // 이미 처리된 신청이면 거부
    if (programJoin.getJoinStatus() != JoinStatus.PENDING) {
      throw new CustomException("이미 처리된 신청입니다.", ErrorCode.BAD_REQUEST);
    }

    Long applicantId = programJoin.getUser().getId();  // 신청자 ID

    // 신청자가 이미 ACTIVE 상태인지 검사
    Optional<ProgramUser> existingUserInProgram =
        programUserRepository.findByUserIdAndProgramIdAndProgramUserStatus(
            applicantId, programId, ProgramUserStatus.ACTIVE);
    if (existingUserInProgram.isPresent()) {
      throw new CustomException("이미 프로그램에 참여한 회원입니다.", ErrorCode.PROGRAM_ALREADY_JOINED);
    }

    // 처리 로직
    Program program = programJoin.getProgram();
    User applicant = programJoin.getUser();
    if (updateProgramJoinStateRequestDto.getIsAccepted().equals("ACCEPTED")) {

      // 신청 승인 → 프로그램 유저 등록
      GroupsUser groupsUser = GroupsUser.create(ProgramUserStatus.ACTIVE, program, applicant, GroupRole.USER);
      groupUserRepository.save(groupsUser);

      // program_join 상태 업데이트
      programJoin.updateJoinStatus(JoinStatus.ACCEPTED);
      programJoinRepository.save(programJoin);

    } else if (updateProgramJoinStateRequestDto.getIsAccepted().equals("DENIED")) {

      programJoin.updateJoinStatus(JoinStatus.DENIED);
      programJoinRepository.save(programJoin);

    }

    // 신청한 사람한테 알람 보내는 로직 나중에 추가
  }
}
