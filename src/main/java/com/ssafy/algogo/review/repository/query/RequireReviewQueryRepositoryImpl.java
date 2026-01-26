package com.ssafy.algogo.review.repository.query;


import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.ssafy.algogo.problem.entity.QProblem.problem;
import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.review.entity.QReview.review;
import static com.ssafy.algogo.review.entity.QRequireReview.requireReview;
import static com.ssafy.algogo.submission.entity.QAlgorithm.algorithm;
import static com.ssafy.algogo.submission.entity.QSubmission.submission;
import com.ssafy.algogo.submission.entity.QSubmission;
import static com.ssafy.algogo.submission.entity.QSubmissionAlgorithm.submissionAlgorithm;
import static com.ssafy.algogo.user.entity.QUser.user;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.review.dto.response.CodeReviewSubmissionInfoDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewResponseDto;
import com.ssafy.algogo.submission.dto.ReviewRematchTargetQueryDto;
import com.ssafy.algogo.submission.dto.response.AlgorithmResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RequireReviewQueryRepositoryImpl implements RequireReviewQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<RequiredCodeReviewResponseDto> getRequiredReviews(Long userId) {

        QSubmission subjectSubmission = new QSubmission("subjectSubmission");

        Expression<Long> reviewCountExpr =
            JPAExpressions
                .select(review.count())
                .from(review)
                .where(review.submission.eq(submission));

        return query
            .from(requireReview)
            .join(requireReview.targetSubmission, submission)
            .join(requireReview.subjectSubmission, subjectSubmission)
            .join(submission.programProblem, programProblem)
            .leftJoin(submissionAlgorithm).on(submissionAlgorithm.submission.eq(submission))
            .leftJoin(submissionAlgorithm.algorithm, algorithm)
            .join(programProblem.problem, problem)
            .join(programProblem.program, program)
            .where(
                requireReview.subjectUser.id.eq(userId),
                requireReview.isDone.isFalse()
            )
            .transform(
                com.querydsl.core.group.GroupBy.groupBy(submission.id).list(
                    Projections.constructor(
                        RequiredCodeReviewResponseDto.class,
                        problem.title,
                        problem.platformType,
                        program.programType.name,
                        program.title,
                        Projections.constructor(
                            CodeReviewSubmissionInfoDto.class,
                            submission.id,
                            submission.language,
                            submission.createdAt,
                            submission.viewCount,
                            com.querydsl.core.group.GroupBy.set(
                                Projections.constructor(
                                    AlgorithmResponseDto.class,
                                    algorithm.id,
                                    algorithm.name
                                )
                            ),
                            reviewCountExpr,
                            submission.user.nickname
                        ),
                        subjectSubmission.createdAt
                    )
                )
            );
    }

    @Override
    public List<ReviewRematchTargetQueryDto> findAllReviewRematchTargets(Long submissionId) {
        return query
            .from(requireReview)
            .join(requireReview.subjectSubmission)
            .leftJoin(submissionAlgorithm)
            .on(submissionAlgorithm.submission.eq(requireReview.subjectSubmission))
            .where(
                requireReview.targetSubmission.id.eq(submissionId),
                requireReview.isDone.eq(false)
            )
            .transform(
                groupBy(requireReview.subjectSubmission).list(
                    Projections.constructor(
                        ReviewRematchTargetQueryDto.class,
                        requireReview.subjectSubmission,
                        list(submissionAlgorithm)
                    )
                )
            );
    }

    @Override
    @Transactional
    public void deleteRequiredReviewsByUserAndProgram(Long userId, Long programId) {
        query
            .delete(requireReview)
            .where(
                requireReview.subjectUser.id.eq(userId),
                requireReview.subjectSubmission.id.in(
                    JPAExpressions
                        .select(submission.id)
                        .from(submission)
                        .innerJoin(submission.programProblem, programProblem)
                        .where(programProblem.program.id.eq(programId))
                )
            )
            .execute();
    }
}