package com.ssafy.algogo.review.repository.query;


import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.ssafy.algogo.problem.entity.QProblem.problem;
import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.review.entity.QRequireReview.requireReview;
import static com.ssafy.algogo.submission.entity.QSubmission.submission;
import static com.ssafy.algogo.submission.entity.QSubmissionAlgorithm.submissionAlgorithm;
import static com.ssafy.algogo.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewResponseDto;
import com.ssafy.algogo.submission.dto.ReviewRematchTargetQueryDto;
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

        return query
            .select(Projections.constructor(
                RequiredCodeReviewResponseDto.class,
                submission.id,
                problem.title,
                program.programType.name,
                program.title,
                submission.user.nickname

            ))
            .from(requireReview)
            .join(requireReview.targetSubmission, submission)
            .join(submission.programProblem, programProblem)
            .join(programProblem.problem, problem)
            .join(programProblem.program, program)
            .join(requireReview.subjectUser, user)
            .where(requireReview.subjectUser.id.eq(userId))
            .where(requireReview.isDone.isFalse())
            .fetch();
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