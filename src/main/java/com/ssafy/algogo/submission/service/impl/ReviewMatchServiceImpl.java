package com.ssafy.algogo.submission.service.impl;

import com.ssafy.algogo.alarm.entity.AlarmPayload;
import com.ssafy.algogo.alarm.service.AlarmService;
import com.ssafy.algogo.review.entity.RequireReview;
import com.ssafy.algogo.review.repository.RequireReviewRepository;
import com.ssafy.algogo.submission.dto.ReviewCandidateQueryDto;
import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.submission.repository.SubmissionRepository;
import com.ssafy.algogo.submission.service.ReviewMatchService;
import com.ssafy.algogo.submission.utils.ReviewMatchRanker;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewMatchServiceImpl implements ReviewMatchService {

    private final ReviewMatchRanker reviewMatchRanker;
    private final SubmissionRepository submissionRepository;
    private final RequireReviewRepository requireReviewRepository;
    private final AlarmService alarmService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void matchReviewers(Submission subjectSubmission, List<Algorithm> subjectAlgorithmList,
        int assignCount) {
        List<ReviewCandidateQueryDto> reviewMatchCandidates = submissionRepository.findReviewMatchCandidates(
            subjectSubmission.getId(), subjectSubmission.getUser().getId(),
            subjectSubmission.getProgramProblem().getId(), subjectSubmission.getLanguage());

        // 후보군에 후보가 존재할 때 (존재하지 않다면 패스)
        if (reviewMatchCandidates.isEmpty()) {
            // 후처리 -> dlq 보낼 수는 있을거임 dead-letter <- Queue 적재  <<< log를 잡아두면, 개발자 볼 수 있지 않을까?
            // dlq에 매칭해야되는 정보도 저장되어있고, log
            return;
        }

        List<Submission> targetSubmissions;
        // 실제 할당할 개수
        int actualAssignCount = Math.min(assignCount, reviewMatchCandidates.size());

        // 후보군에 후보 수가 할당해야하는 수보다 작거나 같을 때
        if (reviewMatchCandidates.size() <= actualAssignCount) {
            targetSubmissions = reviewMatchCandidates.stream()
                .map(ReviewCandidateQueryDto::submission).toList();
        } else {
            List<Submission> rankedSubmissions = reviewMatchRanker.rankReviewerCandidates(
                subjectSubmission, subjectAlgorithmList, reviewMatchCandidates);

            targetSubmissions = rankedSubmissions.subList(0, actualAssignCount);
        }

        List<RequireReview> requireReviewList = targetSubmissions.stream().map(
                target -> RequireReview.builder().subjectSubmission(subjectSubmission)
                    .subjectUser(subjectSubmission.getUser()).targetSubmission(target).build())
            .toList();
        requireReviewRepository.saveAll(requireReviewList);

        for (RequireReview requireReview : requireReviewList) {
            alarmService.createAndSendAlarm(
                subjectSubmission.getUser().getId(),
                "REQUIRED_REVIEW",
                new AlarmPayload(requireReview.getTargetSubmission().getId(), null,
                    subjectSubmission.getProgramProblem().getId(), null, null),
                String.format(
                    "유저(user_id = %d)에게 제출(target_submission_id = %d)건에 요구된 리뷰가 매칭되었습니다. ",
                    subjectSubmission.getUser().getId(),
                    requireReview.getTargetSubmission().getId())
            );
        }
    }
}
