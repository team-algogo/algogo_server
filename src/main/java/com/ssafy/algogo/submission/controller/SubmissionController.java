package com.ssafy.algogo.submission.controller;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.submission.dto.request.SubmissionRequestDto;
import com.ssafy.algogo.submission.dto.request.UserSubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.SubmissionAuthorActiveResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionAuthorStatusResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionListResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionMePageResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionStatsInfosResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionStatsPageResponseDto;
import com.ssafy.algogo.submission.dto.response.TrendIdsResponseDto;
import com.ssafy.algogo.submission.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("/{submissionId}")
    public SuccessResponse getSubmission(
        @AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long submissionId
    ) {
        SubmissionResponseDto submissionResponseDto = submissionService.getSubmission(customUserDetails.getUserId(), submissionId);
        return new SuccessResponse("제출 조회를 성공했습니다.", submissionResponseDto);
    }

    @GetMapping("/{submissionId}/active")
    public SuccessResponse getSubmissionAuthorActive(
        @PathVariable Long submissionId) { // 아직 명확히 test는 안해봄. 추후 실제로 테스트 필요
        SubmissionAuthorActiveResponseDto submissionAuthorActiveResponseDto = submissionService.getSubmissionAuthorActive(
            submissionId);
        String message = null;
        if (submissionAuthorActiveResponseDto.isActive()) {
            message = "작성자가 활성화 상태입니다.";
        } else {
            message = "작성자가 활성화 상태가 아닙니다.";
        }
        return new SuccessResponse(message, submissionAuthorActiveResponseDto);
    }

    @PostMapping
    public SuccessResponse createSubmission(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestBody @Valid SubmissionRequestDto submissionRequestDto) {
        log.info("SubmissionRequestDto : {}", submissionRequestDto.toString());
        SubmissionResponseDto submissionResponseDto = submissionService.createSubmission(
            customUserDetails.getUserId(),
            submissionRequestDto);

        return new SuccessResponse("코드 제출을 성공했습니다.", submissionResponseDto);
    }

    @DeleteMapping("/{submissionId}")
    public SuccessResponse deleteSubmission(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long submissionId
    ) {
        submissionService.deleteSubmission(
            customUserDetails.getUserId(),
            submissionId);
        return new SuccessResponse("제출 삭제를 성공했습니다.", null);
    }

    @GetMapping("/{submissionId}/histories")
    public SuccessResponse getSubmissionHistories(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long submissionId) {
        SubmissionListResponseDto submissionHistories = submissionService.getSubmissionHistories(
            customUserDetails.getUserId(),
            submissionId);
        return new SuccessResponse("제출 히스토리 조회를 성공했습니다.", submissionHistories);
    }

    @GetMapping("/me")
    public SuccessResponse getSubmissionMe(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestParam(value = "language", required = false) String language,
        @RequestParam(value = "isSuccess", required = false) Boolean isSuccess,
        @RequestParam(value = "programType", required = false) String programType,
        @RequestParam(value = "algorithm", required = false) String algorithm,
        @RequestParam(value = "platform", required = false) String platform,
        @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
        @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection,
        @RequestParam(value = "page", defaultValue = "0") Integer page,
        @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        UserSubmissionRequestDto userSubmissionRequestDto = UserSubmissionRequestDto.builder()
            .language(language)
            .isSuccess(isSuccess)
            .programType(programType)
            .algorithm(algorithm)
            .platform(platform)
            .build();

        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        SubmissionMePageResponseDto submissionMe = submissionService.getSubmissionMe(
            customUserDetails.getUserId(),
            userSubmissionRequestDto, pageable);
        return new SuccessResponse("내 제출 조회를 성공했습니다.", submissionMe);
    }

    @GetMapping("/trends")
    public SuccessResponse getTrendingSubmissions(
        @RequestParam(value = "type", defaultValue = "hot") String trendType
    ) {
        TrendIdsResponseDto trendIdsResponseDto = submissionService.getTrendIds(
            trendType.toLowerCase().trim());
        return new SuccessResponse("트렌드 제출 조회를 성공했습니다.", trendIdsResponseDto);
    }

    @GetMapping("/stats/{programProblemId}/lists")
    public SuccessResponse getSubmissionStatsLists(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long programProblemId,
        @RequestParam(value = "language", required = false) String language,
        @RequestParam(value = "isSuccess", required = false) Boolean isSuccess,
        @RequestParam(value = "programType", required = false) String programType,
        @RequestParam(value = "algorithm", required = false) String algorithm,
        @RequestParam(value = "platform", required = false) String platform,
        @RequestParam(value = "nickname", required = false) String nickname,
        @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
        @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection,
        @RequestParam(value = "page", defaultValue = "0") Integer page,
        @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        UserSubmissionRequestDto userSubmissionRequestDto = UserSubmissionRequestDto.builder()
            .language(language)
            .isSuccess(isSuccess)
            .programType(programType)
            .algorithm(algorithm)
            .platform(platform)
            .nickname(nickname)
            .build();

        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        SubmissionStatsPageResponseDto submissionStatsPageResponseDto = submissionService.getSubmissionStatsLists(
            customUserDetails.getUserId(), programProblemId, userSubmissionRequestDto, pageable
        );
        return new SuccessResponse("프로그램 문제의 제출 조회를 성공했습니다.", submissionStatsPageResponseDto);
    }

    @GetMapping("/stats/{programProblemId}")
    public SuccessResponse getSubmissionStats(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long programProblemId
    ) {
        SubmissionStatsInfosResponseDto submissionStatsInfos = submissionService.getSubmissionStatsInfos(
            customUserDetails.getUserId(), programProblemId);

        return new SuccessResponse("프로그램 문제의 제출 통계 조회를 성공했습니다.", submissionStatsInfos);
    }

    @GetMapping("/more/{programId}")
    public SuccessResponse getUserSubmissionStatus(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long programId
    ){
        SubmissionAuthorStatusResponseDto canMoreSubmission = submissionService.canUserMoreSubmission(
            customUserDetails.getUserId(), programId);
        return new SuccessResponse("유저의 추가 제출 가능 여부 조회를 성공했습니다.", canMoreSubmission);
    }
}
