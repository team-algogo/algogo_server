package com.ssafy.algogo.program.group.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.program.entity.ProgramType;
import com.ssafy.algogo.program.group.dto.request.CheckGroupNameRequestDto;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
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
import com.ssafy.algogo.program.repository.ProgramRepository;
import com.ssafy.algogo.program.repository.ProgramTypeRepository;
import com.ssafy.algogo.program.service.ProgramService;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
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
}
