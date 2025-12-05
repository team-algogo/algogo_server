package com.ssafy.algogo.submission.utils;

import com.ssafy.algogo.submission.dto.ReviewCandidateQueryDto;
import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.entity.Submission;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class ReviewMatchRanker {

    private static final double WEIGHT_AGING = 5.0;
    private static final double WEIGHT_SUCCESS = 4.0;
    private static final double WEIGHT_ALGORITHM = 3.0;
    private static final double WEIGHT_EXEC_TIME = 1.0;
    private static final double WEIGHT_MEMORY = 1.0;
    private static final double WEIGHT_VIEW_COUNT = 2.0;
    private static final double WEIGHT_REVIEW_COUNT = 3.0;
    private static final double WEIGHT_REQUIRE_REVIEW_COUNT = 4.0;

    public List<Submission> rankReviewerCandidates(
        Submission subjectSubmission,
        List<Algorithm> subjectAlgorithms,
        List<ReviewCandidateQueryDto> candidateList
    ) {
        long minAge = Long.MAX_VALUE;
        long maxAge = Long.MIN_VALUE;
        long minExecDiff = Long.MAX_VALUE;
        long maxExecDiff = Long.MIN_VALUE;
        long minMemDiff = Long.MAX_VALUE;
        long maxMemDiff = Long.MIN_VALUE;
        long minVieCount = Long.MAX_VALUE;
        long maxVieCount = Long.MIN_VALUE;
        long minReviewCount = Long.MAX_VALUE;
        long maxReviewCount = Long.MIN_VALUE;
        long minRequireReviewCount = Long.MAX_VALUE;
        long maxRequireReviewCount = Long.MIN_VALUE;

        List<ScoredSubmission> scoredSubmissionList = new ArrayList<>(candidateList.size());

        Set<Long> subjectAlgorithmIdSet = subjectAlgorithms.stream()
            .map(Algorithm::getId)
            .collect(Collectors.toSet());

        for (ReviewCandidateQueryDto candidate : candidateList) {
            Submission candidateSubmission = candidate.submission();
            List<Long> candidateAlgorithmIdList = candidate.algorithmIdList();

            // 해당 후보 제출의 aging, execTimeDiff, memoryDiff, vieCount 측정
            long age = Duration.between(candidateSubmission.getCreatedAt(), LocalDateTime.now())
                .toMillis();
            long execDiff = Math.abs(
                subjectSubmission.getExecTime() - candidateSubmission.getExecTime());
            long memDiff =
                Math.abs(subjectSubmission.getMemory()) - candidateSubmission.getMemory();
            long viewCount = candidateSubmission.getViewCount();
            long reviewCount = candidate.reviewCount();
            long requireReviewCount = candidate.requireReviewCount();

            // 가중치 계산을 위한 정규 분포용 지표
            minAge = Math.min(minAge, age);
            maxAge = Math.max(maxAge, age);
            minExecDiff = Math.min(minExecDiff, execDiff);
            maxExecDiff = Math.max(maxExecDiff, execDiff);
            minMemDiff = Math.min(minMemDiff, memDiff);
            maxMemDiff = Math.max(maxMemDiff, memDiff);
            minVieCount = Math.min(minVieCount, viewCount);
            maxVieCount = Math.max(maxVieCount, viewCount);
            minReviewCount = Math.min(minReviewCount, reviewCount);
            maxReviewCount = Math.max(maxReviewCount, reviewCount);
            minRequireReviewCount = Math.min(minRequireReviewCount, requireReviewCount);
            maxRequireReviewCount = Math.max(maxRequireReviewCount, requireReviewCount);

            scoredSubmissionList.add(
                ScoredSubmission.builder()
                    .submission(candidateSubmission)
                    .candidateAlgorithms(new HashSet<>(candidateAlgorithmIdList))
                    .age(age)
                    .execDiff(execDiff)
                    .memDiff(memDiff)
                    .viewCount(viewCount)
                    .reviewCount(reviewCount)
                    .requireReviewCount(requireReviewCount)
                    .build()
            );
        }

        // score 계산
        for (ScoredSubmission scoredSubmission : scoredSubmissionList) {
            scoredSubmission.computeTotalScore(subjectSubmission, subjectAlgorithmIdSet, minAge,
                maxAge, maxExecDiff, maxMemDiff, minVieCount,
                maxVieCount, minReviewCount, maxReviewCount, minRequireReviewCount,
                maxRequireReviewCount);
        }

        // score 기준 정렬
        return scoredSubmissionList.stream()
            .sorted((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()))
            .map(ScoredSubmission::getSubmission)
            .collect(Collectors.toList());
    }


    /**
     * 리뷰 매칭 후보군의 score 정보 객체
     */
    @Getter
    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class ScoredSubmission {

        private final Submission submission;
        private final long age;
        private final long execDiff;
        private final long memDiff;
        private final long viewCount;
        private final long reviewCount;
        private final long requireReviewCount;
        private final Set<Long> candidateAlgorithms;

        private double totalScore;

        public void computeTotalScore(
            Submission subjectSubmission,
            Set<Long> subjectAlgorithms,
            long minAge,
            long maxAge,
            long maxExecDiff,
            long maxMemDiff,
            long minViewCount,
            long maxViewCount,
            long minReviewCount,
            long maxReviewCount,
            long minRequireReviewCount,
            long maxRequireReviewCount
        ) {
            // score 계산 부분
            // [최신 제출 = 0 ~ 오래된 제출 = 1]
            double ageScore = normalize(this.age, minAge, maxAge);
            // [성공&성공 || 실패&실패 = 0 ~ 성공&실패 = 1]
            double isSuccessScore =
                getIsSuccessDiff(subjectSubmission, this.submission) ? 1.0 : 0.0;
            // [유사시 = 0 ~ 상이시 = 1]
            double algoDiffScore = getAlgoDiff(subjectAlgorithms);
            // [유사시 = 0 ~ 상이시 = 1]
            double execTimeScore =
                (maxExecDiff == 0L) ? 0.0 : (double) execDiff / (double) maxExecDiff;
            // [유사시 = 0 ~ 상이시 = 1]
            double memoryScore =
                (maxMemDiff == 0L) ? 0.0 : (double) memDiff / (double) maxMemDiff;
            // [높으면 = 0 ~ 낮으면 = 1]
            double viewCountScore = 1.0 - normalize(viewCount, minViewCount, maxViewCount);
            // [높으면 = 0 ~ 낮으면 = 1]
            double reviewCountScore = 1.0 - normalize(reviewCount, minReviewCount, maxReviewCount);
            // [높으면 = 0 ~ 낮으면 = 1]
            double requireReviewCountScore =
                1.0 - normalize(requireReviewCount, minRequireReviewCount, maxRequireReviewCount);

            this.totalScore =
                WEIGHT_AGING * ageScore
                    + WEIGHT_SUCCESS * isSuccessScore
                    + WEIGHT_ALGORITHM * algoDiffScore
                    + WEIGHT_EXEC_TIME * execTimeScore
                    + WEIGHT_MEMORY * memoryScore
                    + WEIGHT_VIEW_COUNT * viewCountScore
                    + WEIGHT_REVIEW_COUNT * reviewCountScore
                    + WEIGHT_REQUIRE_REVIEW_COUNT * requireReviewCountScore;
        }

        // 각 개체의 option 들을 0~1 사이로 정규화 시킴

        // 제출 기준 [최신순, 조회수높은순, 리뷰많은순, 요구된 리뷰 많은순  0 ~ 1]
        private double normalize(long value, long minValue, long maxValue) {
            if (maxValue == minValue) {
                return 0.5;
            }
            return (double) (value - minValue) / (double) (maxValue - minValue);
        }

        // 제출 성공여부 기준 [성공&성공 || 실패&실패 = 0 ~ 성공&실패 = 1]
        private boolean getIsSuccessDiff(Submission subjectSubmission,
            Submission candidateSubmission) {
            Boolean subjectIsSuccess = subjectSubmission.getIsSuccess();
            Boolean candidateIsSuccess = candidateSubmission.getIsSuccess();

            if ((subjectIsSuccess == null) || (candidateIsSuccess == null)) {
                return false;
            }
            return !subjectIsSuccess.equals(candidateIsSuccess);
        }

        // 제출 알고리즘 유사도 기준 [유사시 = 0 ~ 상이시 = 1]
        private double getAlgoDiff(
            Set<Long> subjectAlgorithms
        ) {
            Set<Long> union = new HashSet<>(subjectAlgorithms);
            union.addAll(this.candidateAlgorithms);
            if (union.isEmpty()) {
                return 0.0;
            }

            Set<Long> inter = new HashSet<>(subjectAlgorithms);
            inter.retainAll(candidateAlgorithms);

            double similarity = (double) inter.size() / (double) union.size();
            return 1.0 - similarity;
        }
    }
}
