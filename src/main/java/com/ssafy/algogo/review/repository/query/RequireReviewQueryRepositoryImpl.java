package com.ssafy.algogo.review.repository.query;


import static com.ssafy.algogo.problem.entity.QProblem.problem;
import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.review.entity.QRequireReview.requireReview;
import static com.ssafy.algogo.submission.entity.QSubmission.submission;
import static com.ssafy.algogo.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
                user.nickname

            ))
            .from(requireReview)
            .join(requireReview.submission, submission)
            .join(submission.programProblem, programProblem)
            .join(programProblem.problem, problem)
            .join(programProblem.program, program)
            .join(requireReview.user, user)
            .where(requireReview.user.id.eq(userId))
            .fetch();
    }
}