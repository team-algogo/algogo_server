package com.ssafy.algogo.review.repository.query;

import static com.ssafy.algogo.problem.entity.QProblem.problem;
import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.review.entity.QReview.review;
import static com.ssafy.algogo.submission.entity.QSubmission.submission;
import static com.ssafy.algogo.user.entity.QUser.user;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.review.dto.response.ReceiveCodeReviewResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public Page<ReceiveCodeReviewResponseDto> getReceiveReviews(Long userId, Integer page,
        Integer size) {

        List<ReceiveCodeReviewResponseDto> content = query.
            select(Projections.constructor(
                ReceiveCodeReviewResponseDto.class,
                submission.id,
                problem.title,
                program.programType.name,
                program.title,
                user.nickname,
                Expressions.stringTemplate(
                    "CONCAT(SUBSTRING({0}, 1, 15), '...')",
                    review.content
                ),
                review.modifiedAt
            ))
            .from(review)
            .join(review.submission, submission)
            .join(submission.programProblem, programProblem)
            .join(programProblem.problem, problem)
            .join(submission.user, user)
            .where(user.id.eq(userId))
            .orderBy(review.modifiedAt.desc())
            .offset((long) page * size)
            .limit(size)
            .fetch();

        Long total = query.
            select(review.count())
            .from(review)
            .join(review.submission, submission)
            .join(submission.user, user)
            .where(user.id.eq(userId))
            .fetchOne();

        return new PageImpl<>(
            content,
            PageRequest.of(page, size),
            total == null ? 0 : total
        );
    }
}