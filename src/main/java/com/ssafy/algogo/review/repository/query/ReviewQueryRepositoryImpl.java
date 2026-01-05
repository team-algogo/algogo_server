
package com.ssafy.algogo.review.repository.query;

import static com.ssafy.algogo.problem.entity.QProblem.problem;
import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.review.entity.QReview.review;
import static com.ssafy.algogo.review.entity.QUserReviewReaction.userReviewReaction;
import static com.ssafy.algogo.submission.entity.QSubmission.submission;
import static com.ssafy.algogo.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.review.dto.response.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.dto.response.UserCodeReviewResponseDto;
import java.util.ArrayList;
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
    public List<CodeReviewTreeResponseDto> getReviewsBySubmissionId(Long userId,
        Long submissionId) {

        BooleanExpression likedByMeExpr =
            JPAExpressions
                .selectOne()
                .from(userReviewReaction)
                .where(
                    userReviewReaction.review.eq(review),
                    userReviewReaction.user.id.eq(userId)
                )
                .exists();

        return query.select(Projections.constructor(
                CodeReviewTreeResponseDto.class,
                review.id,
                review.parentReview.id,
                user.id,
                submission.id,
                review.likeCount,
                review.codeLine,
                review.content,
                review.createdAt,
                review.modifiedAt,
                likedByMeExpr
            ))
            .from(review)
            .join(review.user, user)
            .where(review.submission.id.eq(submissionId))
            .orderBy(review.createdAt.asc())
            .fetch();
    }

    @Override
    public Page<UserCodeReviewResponseDto> getReceiveReviews(Long userId, Integer page,
        Integer size) {

        List<UserCodeReviewResponseDto> content = query.
            select(Projections.constructor(
                UserCodeReviewResponseDto.class,
                submission.id,
                problem.title,
                program.programType.name,
                program.title,
                review.user.nickname,
                Expressions.stringTemplate(
                    "CASE " +
                        "WHEN LENGTH({0}) > 15 THEN CONCAT(SUBSTRING({0}, 1, 15), '...') " +
                        "ELSE {0} " +
                        "END",
                    review.content
                )
                ,
                review.modifiedAt
            ))
            .from(review)
            .join(review.submission, submission)
            .join(submission.programProblem, programProblem)
            .join(programProblem.problem, problem)
            .join(submission.user, user)
            .where(user.id.eq(userId))
            .where(review.user.id.ne(userId))
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
            .where(review.user.id.ne(userId))
            .fetchOne();

        return new PageImpl<>(
            content,
            PageRequest.of(page, size),
            total == null ? 0 : total
        );
    }

    @Override
    public Page<UserCodeReviewResponseDto> getDoneReviews(Long userId, Integer page, Integer size) {

        List<UserCodeReviewResponseDto> content = query.
            select(Projections.constructor(
                UserCodeReviewResponseDto.class,
                submission.id,
                problem.title,
                program.programType.name,
                program.title,
                submission.user.nickname,
                Expressions.stringTemplate(
                    "CASE " +
                        "WHEN LENGTH({0}) > 15 THEN CONCAT(SUBSTRING({0}, 1, 15), '...') " +
                        "ELSE {0} " +
                        "END",
                    review.content
                )
                ,
                review.modifiedAt
            ))
            .from(review)
            .join(review.submission, submission)
            .join(submission.programProblem, programProblem)
            .join(programProblem.problem, problem)
            .where(
                review.user.id.eq(userId),
                submission.user.id.ne(userId)
            )
            .orderBy(review.modifiedAt.desc())
            .offset((long) page * size)
            .limit(size)
            .fetch();

        Long total = query
            .select(review.count())
            .from(review)
            .join(review.submission, submission)
            .where(
                review.user.id.eq(userId),
                submission.user.id.ne(userId)
            )
            .fetchOne();

        return new PageImpl<>(
            content,
            PageRequest.of(page, size),
            total == null ? 0 : total
        );
    }
}
