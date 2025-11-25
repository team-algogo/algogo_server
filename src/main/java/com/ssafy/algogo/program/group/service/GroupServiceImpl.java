package com.ssafy.algogo.program.group.service;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.program.entity.ProgramType;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import com.ssafy.algogo.program.group.entity.GroupRole;
import com.ssafy.algogo.program.group.entity.GroupRoom;
import com.ssafy.algogo.program.group.entity.GroupsUser;
import com.ssafy.algogo.program.group.entity.ProgramStatus;
import com.ssafy.algogo.program.group.repository.GroupRepository;
import com.ssafy.algogo.program.group.repository.GroupUserRepository;
import com.ssafy.algogo.program.repository.ProgramTypeRepository;
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
public class GroupServiceImpl implements GroupService{

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final ProgramTypeRepository programTypeRepository;
  private final GroupUserRepository groupUserRepository;

//  @Override
//  @Transactional(readOnly = true)
//  public GroupRoomResponseDto getGroupRoomDetail(Long programId) {
//    Optional<GroupRoom> groupRoom = groupRepository.findById(programId);
//    if(groupRoom.isPresent()){
//      GroupRoom nowRoom = groupRoom.get();
//      return new GroupRoomResponseDto(nowRoom.getId(), nowRoom.getTitle(), nowRoom.getDescription(), nowRoom.getCreatedAt(), nowRoom.getModifiedAt(),
//          nowRoom.getCapacity(), 0L);
//    }else{
//      // 예외처리 <- 없는 그룹방입니다
//      return new GroupRoomResponseDto(1L, "temp", "temp", LocalDateTime.now(), LocalDateTime.now(),
//          0L, 0L);
//    }
//  }

  @Override
  public GroupRoomResponseDto createGroupRoom(Long userId, CreateGroupRoomRequestDto createGroupRoomRequestDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException("userId에 해당하는 데이터가 DB에 없습니다.",
            ErrorCode.USER_NOT_FOUND));

  ProgramType programType = programTypeRepository.findByName("group")
      .orElseThrow(() -> new CustomException("group에 해당하는 데이터가 DB에 없습니다.", ErrorCode.PROGRAM_TYPE_NOT_FOUND));

    GroupRoom groupRoom = GroupRoom.create(
        createGroupRoomRequestDto.getTitle(),
        createGroupRoomRequestDto.getDescription(),
        programType,
        createGroupRoomRequestDto.getCapacity()
    );
    groupRepository.save(groupRoom);

    GroupsUser groupsUser = GroupsUser.create(
        ProgramStatus.ACTIVE,
        groupRoom,
        user,
        GroupRole.ADMIN
    );
    groupUserRepository.save(groupsUser);

    return groupRepository.getGroupRoomDetail(groupRoom.getId());
  }
}
