package com.ssafy.algogo.submission.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.common.utils.S3Service;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.review.repository.RequireReviewRepository;
import com.ssafy.algogo.submission.dto.ReviewRematchTargetQueryDto;
import com.ssafy.algogo.submission.dto.request.SubmissionRequestDto;
import com.ssafy.algogo.submission.dto.request.UserSubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.SubmissionAuthorActiveResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionListResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;
import com.ssafy.algogo.submission.dto.response.TrendIdsResponseDto;
import com.ssafy.algogo.submission.dto.response.UserSubmissionPageResponseDto;
import com.ssafy.algogo.submission.dto.response.UserSubmissionResponseDto;
import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.submission.entity.SubmissionAlgorithm;
import com.ssafy.algogo.submission.event.SubmissionAiEvaluationEvent;
import com.ssafy.algogo.submission.event.SubmissionEvent;
import com.ssafy.algogo.submission.event.SubmissionRematchEvent;
import com.ssafy.algogo.submission.repository.AlgorithmRepository;
import com.ssafy.algogo.submission.repository.SubmissionAlgorithmRepository;
import com.ssafy.algogo.submission.repository.SubmissionRepository;
import com.ssafy.algogo.submission.service.ReviewMatchService;
import com.ssafy.algogo.submission.service.SubmissionService;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ProgramProblemRepository programProblemRepository;
    private final SubmissionAlgorithmRepository submissionAlgorithmRepository;
    private final AlgorithmRepository algorithmRepository;
    private final RequireReviewRepository requireReviewRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ReviewMatchService reviewMatchService;
    private final S3Service s3Service;

    @Override
    public SubmissionResponseDto getSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId).orElseThrow(
            () -> new CustomException("존재하지 않는 제출입니다.", ErrorCode.SUBMISSION_NOT_FOUND));

        List<Algorithm> usedAlgorithmList = algorithmRepository.findAllAlgorithmsBySubmissionId(
            submission.getId());

        submission.increaseViewCount();

        return SubmissionResponseDto.from(submission, usedAlgorithmList);
    }

    @Override
    public SubmissionResponseDto createSubmission(Long userId,
        SubmissionRequestDto submissionRequestDto) {
        // userId가 필요한 모든 곳에서 사용할텐데 userId에 관한 검증은 aop로 뺄까?
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("존재하지 않는 회원입니다.", ErrorCode.USER_NOT_FOUND));
        ProgramProblem programProblem = programProblemRepository.findById(
            submissionRequestDto.getProgramProblemId()).orElseThrow(
            () -> new CustomException("존재하지 않는 프로그램 문제입니다.", ErrorCode.PROGRAM_PROBLEM_NOT_FOUND));

        // 코드는 S3에 저장
        String s3CodeUrl = s3Service.uploadText(userId, submissionRequestDto.getCode());

        // 제출 저장
        Submission submission = submissionRepository.save(
            Submission.builder().language(submissionRequestDto.getLanguage()).code(s3CodeUrl)
                .execTime(submissionRequestDto.getExecTime())
                .memory(submissionRequestDto.getMemory())
                .strategy(submissionRequestDto.getStrategy())
                .isSuccess(submissionRequestDto.getIsSuccess()).user(user)
                .programProblem(programProblem).build());

        // 프로그램 문제 제출 수 +1
        programProblem.increaseSubmissionCount();

        // 프로그램 문제 풀이 수 +1
        if (submission.getIsSuccess().equals(Boolean.TRUE)) {
            programProblem.increaseSolvedCount();
        }

        // 제출 시 사용한 알고리즘 저장
        List<Algorithm> usedAlgorithmList = createSubmissionAlgorithmAndFetch(submissionRequestDto,
            submission);

//        // 리뷰 매칭
//        reviewMatchService.matchReviewers(submission, usedAlgorithmList, 2);

        // 비동기 적용을 위해 Event 발행
        applicationEventPublisher.publishEvent(
            new SubmissionEvent(submission, usedAlgorithmList, 2));

        // ai 점수 평가 Event 발행(비동기, 트랜잭션 분리)
        applicationEventPublisher.publishEvent(
            new SubmissionAiEvaluationEvent(submission.getId())
        );

        return SubmissionResponseDto.from(submission, usedAlgorithmList);
    }

    @Override
    public void deleteSubmission(Long userId, Long submissionId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("존재하지 않는 회원입니다.", ErrorCode.USER_NOT_FOUND));

        Submission submission = submissionRepository.findById(submissionId).orElseThrow(
            () -> new CustomException("존재하지 않는 제출입니다.", ErrorCode.SUBMISSION_NOT_FOUND));

        if (!submission.getUser().equals(user)) {
            throw new CustomException("제출과 회원 정보가 일치하지 않습니다.", ErrorCode.INVALID_PARAMETER);
        }

        // 요구된 리뷰 중 수행 X 조회
        List<ReviewRematchTargetQueryDto> reviewRematchTargetList = requireReviewRepository.findAllReviewRematchTargets(
            submission.getId());
        // 원 제출 삭제 -> 요구된 리뷰 삭제(@OnCascade.DELETE)
        submissionRepository.delete(submission);

        // 리뷰 리매칭
//        for (ReviewRematchTargetQueryDto target : reviewRematchTargetList) {
//            reviewMatchService.matchReviewers(target.submission(), target.algorithmList(), 1);
//        }

        // 리매칭 트랜잭션 분리
        applicationEventPublisher.publishEvent(new SubmissionRematchEvent(reviewRematchTargetList));
    }

    private List<Algorithm> createSubmissionAlgorithmAndFetch(
        SubmissionRequestDto submissionRequestDto, Submission submission) {
        List<Long> usedAlgorithmRequestIdList = submissionRequestDto.getAlgorithmList();

        // SubmissionAlgorithm create 요청 알고리즘 검증을 위한 조회
        List<Algorithm> usedAlgorithmList = algorithmRepository.findAllById(
            new HashSet<>(usedAlgorithmRequestIdList));

        // 중복 제거 후 사이즈와 실제 DB 조회 사이즈가 다르면 없는 알고리즘이 요청에 섞인 경우임.
        if (new HashSet<>(usedAlgorithmRequestIdList).size() != usedAlgorithmList.size()) {
            throw new CustomException("잘못된 알고리즘 ID 정보가 포함되어 있습니다.", ErrorCode.BAD_REQUEST);
        }

        // SubmissionAlgorithm 저장
        submissionAlgorithmRepository.saveAll(usedAlgorithmList.stream().map(
            usedAlgorithm -> SubmissionAlgorithm.builder().submission(submission)
                .algorithm(usedAlgorithm).build()).toList());

        return usedAlgorithmList;
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionListResponseDto getSubmissionHistories(Long userId, Long submissionId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("존재하지 않는 회원입니다.", ErrorCode.USER_NOT_FOUND));

        Submission submission = submissionRepository.findById(submissionId).orElseThrow(
            () -> new CustomException("존재하지 않는 제출입니다.", ErrorCode.SUBMISSION_NOT_FOUND));
        // 해당 유저의 해당 프로그램 문제의 제출 모두 조회
        List<Submission> submissionHistories = submissionRepository.findAllByUserAndProgramProblemOrderByCreatedAtAsc(
            user, submission.getProgramProblem());

        return new SubmissionListResponseDto(submissionHistories.stream().map(
            history -> SubmissionResponseDto.from(history,
                algorithmRepository.findAllAlgorithmsBySubmissionId(history.getId()))).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserSubmissionPageResponseDto getSubmissionMe(Long userId,
        UserSubmissionRequestDto userSubmissionRequestDto, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("존재하지 않는 회원입니다.", ErrorCode.USER_NOT_FOUND));

        Page<UserSubmissionResponseDto> submissionMeList = submissionRepository.findAllUserSubmissionList(
            user.getId(), userSubmissionRequestDto, pageable);
        return UserSubmissionPageResponseDto.from(submissionMeList);
    }

    @Override
    @Transactional(readOnly = true)
    public TrendIdsResponseDto getTrendIds(String trendType) {
        return switch (trendType) {
            case "hot" ->
                new TrendIdsResponseDto(submissionRepository.findHotSubmissionIds(), null);
            case "recent" ->
                new TrendIdsResponseDto(submissionRepository.findRecentSubmissionIds(), null);
            case "join-in" ->
                new TrendIdsResponseDto(null, submissionRepository.findTrendProgramProblemIds());
            default ->
                throw new CustomException("트렌드 조회 type이 잘못되었습니다.", ErrorCode.INVALID_PARAMETER);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionAuthorActiveResponseDto getSubmissionAuthorActive(Long submissionId) {
        Boolean isActive = submissionRepository.isSubmissionAuthorActive(submissionId);

        if (isActive == null) {
            throw new CustomException(
                "해당 코드 제출이 존재하지 않습니다.",
                ErrorCode.SUBMISSION_NOT_FOUND
            );
        }

        return new SubmissionAuthorActiveResponseDto(isActive);
    }

    @Override
    public UserSubmissionPageResponseDto getSubmissionsByProgramProblem(Long userId,
        Long programProblemId, UserSubmissionRequestDto userSubmissionRequestDto,
        Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("존재하지 않는 회원입니다.", ErrorCode.USER_NOT_FOUND));

        ProgramProblem programProblem = programProblemRepository.findById(programProblemId)
            .orElseThrow(() -> new CustomException("존재하지 않는 프로그램 문제입니다.",
                ErrorCode.PROGRAM_PROBLEM_NOT_FOUND));

        Page<UserSubmissionResponseDto> submissionLists = submissionRepository.findAllSubmissionsByProgramProblem(
            programProblemId,
            userSubmissionRequestDto, pageable);

        return UserSubmissionPageResponseDto.from(submissionLists);
    }
}
