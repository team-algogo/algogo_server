package com.ssafy.algogo.program.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.program.entity.JoinStatus;
import com.ssafy.algogo.program.entity.Program;
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
import java.util.Optional;
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
}
