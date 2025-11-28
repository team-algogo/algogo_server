package com.ssafy.algogo.submission.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.submission.dto.request.SubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.SubmissionListResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.submission.repository.SubmissionRepository;
import com.ssafy.algogo.submission.service.SubmissionService;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

  private final UserRepository userRepository;
  private final SubmissionRepository submissionRepository;
  private final ProgramProblemRepository programProblemRepository;

  @Override
  public SubmissionResponseDto getSubmission(Long submissionId) {
    return SubmissionResponseDto.from(submissionRepository.findById(submissionId)
        .orElseThrow(() -> new CustomException("제출 정보가 잘못 되었습니다.", ErrorCode.INVALID_PARAMETER)));
  }

  @Override
  public SubmissionResponseDto createSubmission(Long userId,
      SubmissionRequestDto submissionRequestDto) {
    // userId가 필요한 모든 곳에서 사용할텐데 userId에 관한 검증은 aop로 뺄까?
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException("존재하지 않는 회원입니다.", ErrorCode.USER_NOT_FOUND));
    ProgramProblem programProblem = programProblemRepository.findById(
        submissionRequestDto.getProgramProblemId()).orElseThrow(
        () -> new CustomException("프로그램 문제 정보가 잘못 되었습니다.", ErrorCode.INVALID_PARAMETER));

    // 알고리즘 테이블과의 연관관계 테이블 로직 필요

    return SubmissionResponseDto.from(submissionRepository.save(
        Submission.builder().language(submissionRequestDto.getLanguage())
            .code(submissionRequestDto.getCode()).execTime(submissionRequestDto.getExecTime())
            .memory(submissionRequestDto.getMemory()).strategy(submissionRequestDto.getStrategy())
            .isSuccess(submissionRequestDto.getIsSuccess()).user(user)
            .programProblem(programProblem).build()));
  }

  @Override
  public SubmissionListResponseDto getSubmissionHistories(Long userId, Long submissionId) {
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new CustomException("존재하지 않는 회원입니다.", ErrorCode.USER_NOT_FOUND));

    Submission submission = submissionRepository.findById(submissionId)
        .orElseThrow(
            () -> new CustomException("프로그램 문제 정보가 잘못 되었습니다.", ErrorCode.INVALID_PARAMETER));
    // 해당 유저의 해당 프로그램 문제의 제출 모두 조회
    List<Submission> submissionHistories = submissionRepository.findAllByUserAndProgramProblemOrderByCreatedAtAsc(
        user,
        submission.getProgramProblem());
    return new SubmissionListResponseDto(
        submissionHistories.stream().map(SubmissionResponseDto::from).toList());
  }
}
