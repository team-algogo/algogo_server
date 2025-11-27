package com.ssafy.algogo.program.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.program.dto.request.ApplyProgramInviteRequestDto;
import com.ssafy.algogo.program.dto.response.GetProgramInviteStateListResponseDto;
import com.ssafy.algogo.program.dto.response.GetProgramInviteStateResponseDto;
import com.ssafy.algogo.program.dto.response.GetProgramJoinStateListResponseDto;
import com.ssafy.algogo.program.dto.response.GetProgramJoinStateResponseDto;
import com.ssafy.algogo.program.entity.InviteStatus;
import com.ssafy.algogo.program.entity.JoinStatus;
import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.entity.ProgramInvite;
import com.ssafy.algogo.program.entity.ProgramJoin;
import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.program.repository.ProgramInviteRepository;
import com.ssafy.algogo.program.repository.ProgramJoinRepository;
import com.ssafy.algogo.program.repository.ProgramRepository;
import com.ssafy.algogo.program.repository.ProgramUserRepository;
import com.ssafy.algogo.program.service.ProgramService;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {

  private final ProgramRepository programRepository;
  private final ProgramUserRepository programUserRepository;
  private final ProgramJoinRepository programJoinRepository;
  private final ProgramInviteRepository programInviteRepository;
  private final UserRepository userRepository;

  @Override
  public void applyProgramJoin(Long userId, Long programId) {

    // 이미 신청한 상태가 PENDING인 경우, conflict 에러 발생
    Optional<ProgramJoin> existingApplication = programJoinRepository.findByUserIdAndProgramIdAndJoinStatus(userId, programId, JoinStatus.PENDING);
    if (existingApplication.isPresent()) {
      throw new CustomException("이미 PENDING 상태로 프로그램 신청이 존재합니다.", ErrorCode.DUPLICATE_RESOURCE);
    }

    // 프로그램에 이미 참여한 사용자가 있는지 확인 (ProgramUserStatus가 ACTIVE인 경우)
    Optional<ProgramUser> existingUserInProgram = programUserRepository.findByUserIdAndProgramIdAndProgramUserStatus(userId, programId, ProgramUserStatus.ACTIVE);
    if (existingUserInProgram.isPresent()) {
      throw new CustomException("이미 프로그램에 참여한 회원입니다.", ErrorCode.PROGRAM_ALREADY_JOINED);
    }

    // 프로그램 신청을 위한 로직
    Program program = programRepository.findById(programId)
        .orElseThrow(() -> new CustomException("프로그램을 찾을 수 없습니다.", ErrorCode.PROGRAM_ID_NOT_FOUND));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

    ProgramJoin programJoin = ProgramJoin.builder()
        .user(user)
        .program(program)
        .joinStatus(JoinStatus.PENDING)
        .build();
    programJoinRepository.save(programJoin);
  }

  @Override
  @Transactional(readOnly = true)
  public GetProgramJoinStateListResponseDto getProgramJoinState(Long programId) {
    //만약 유저 신청 정보가 엄청 많은 경우는 어떻게 처리? <- 이 부분은 나중에 고민

    List<ProgramJoin> programJoins = programJoinRepository.findByProgramIdWithUser(programId);

    List<GetProgramJoinStateResponseDto> userList = programJoins.stream()
        .map(GetProgramJoinStateResponseDto::from)
        .collect(Collectors.toList());

    return new GetProgramJoinStateListResponseDto(userList);
  }

  @Override
  public void applyProgramInvite(Long programId,
      ApplyProgramInviteRequestDto applyProgramInviteRequestDto) {

    // 프로그램에 이미 참여한 사용자가 있는지 확인 (ProgramUserStatus가 ACTIVE인 경우)
    Optional<ProgramUser> existingUserInProgram = programUserRepository.findByUserIdAndProgramIdAndProgramUserStatus(applyProgramInviteRequestDto.getUserId(), programId, ProgramUserStatus.ACTIVE);
    if (existingUserInProgram.isPresent()) {
      throw new CustomException("이미 프로그램에 참여한 회원입니다.", ErrorCode.PROGRAM_ALREADY_JOINED);
    }

    // 이미 PENDING 상태로 초대가 존재하는지 확인
    Optional<ProgramInvite> existingInvite = programInviteRepository.findByProgramIdAndUserIdAndInviteStatus(
        programId, applyProgramInviteRequestDto.getUserId(), InviteStatus.PENDING);

    if (existingInvite.isPresent()) {
      throw new CustomException("이미 초대 신청이 존재합니다.", ErrorCode.DUPLICATE_RESOURCE);
    }

    Program program = programRepository.findById(programId)
        .orElseThrow(() -> new CustomException("프로그램을 찾을 수 없습니다.", ErrorCode.PROGRAM_ID_NOT_FOUND));
    User user = userRepository.findById(applyProgramInviteRequestDto.getUserId())
        .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

    ProgramInvite programInvite = ProgramInvite.builder()
        .program(program)
        .user(user)
        .inviteStatus(InviteStatus.PENDING)
        .build();
    programInviteRepository.save(programInvite);
  }

  @Override
  public void deleteProgramInvite(Long programId, Long inviteId) {
    ProgramInvite programInvite = programInviteRepository.findById(inviteId)
        .orElseThrow(() -> new CustomException("해당 초대 정보를 찾을 수 없습니다.", ErrorCode.PROGRAM_INVITE_NOT_FOUND));

    // 초대가 해당 programId에 속하는지 확인
    if (!programInvite.getProgram().getId().equals(programId)) {
      throw new CustomException("해당 프로그램의 초대가 아닙니다.", ErrorCode.INVALID_PARAMETER);
    }

    // 이미 처리된 초대는 삭제할 수 없음
    if (programInvite.getInviteStatus() != InviteStatus.PENDING) {
      throw new CustomException("이미 처리된 초대는 삭제할 수 없습니다.", ErrorCode.BAD_REQUEST);
    }

    programInviteRepository.delete(programInvite);

    // 음 알림을 삭제해야 한다면 해당 로직 나중에 추가
  }

  @Override
  @Transactional(readOnly = true)
  public GetProgramInviteStateListResponseDto getProgramInviteState(Long programId) {
    //만약 유저 초대 정보가 엄청 많은 경우는 어떻게 처리? <- 이 부분은 나중에 고민

    List<ProgramInvite> programInvites = programInviteRepository.findByProgramIdWithUser(programId);

    List<GetProgramInviteStateResponseDto> userList = programInvites.stream()
        .map(GetProgramInviteStateResponseDto::from)
        .collect(Collectors.toList());

    return new GetProgramInviteStateListResponseDto(userList);
  }
}
