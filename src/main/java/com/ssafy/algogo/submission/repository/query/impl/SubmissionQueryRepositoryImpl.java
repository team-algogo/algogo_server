package com.ssafy.algogo.submission.repository.query.impl;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.ssafy.algogo.problem.entity.QProblem.problem;
import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.program.entity.QProgramType.programType;
import static com.ssafy.algogo.program.entity.QProgramUser.programUser;
import static com.ssafy.algogo.review.entity.QRequireReview.requireReview;
import static com.ssafy.algogo.review.entity.QReview.review;
import static com.ssafy.algogo.submission.entity.QAlgorithm.algorithm;
import static com.ssafy.algogo.submission.entity.QSubmission.submission;
import static com.ssafy.algogo.submission.entity.QSubmissionAlgorithm.submissionAlgorithm;
import static com.ssafy.algogo.user.entity.QUser.user;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.problem.dto.response.ProblemResponseDto;
import com.ssafy.algogo.problem.dto.response.ProgramProblemDetailResponseDto;
import com.ssafy.algogo.problem.entity.PlatformType;
import com.ssafy.algogo.problem.entity.QProblem;
import com.ssafy.algogo.problem.entity.QProgramProblem;
import com.ssafy.algogo.program.dto.response.ProgramResponseDto;
import com.ssafy.algogo.program.dto.response.ProgramTypeResponseDto;
import com.ssafy.algogo.program.entity.QProgram;
import com.ssafy.algogo.program.entity.QProgramType;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.review.entity.QRequireReview;
import com.ssafy.algogo.review.entity.QReview;
import com.ssafy.algogo.submission.dto.ReviewCandidateQueryDto;
import com.ssafy.algogo.submission.dto.request.UserSubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.AlgorithmResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;
import com.ssafy.algogo.submission.dto.response.UserSubmissionResponseDto;
import com.ssafy.algogo.submission.entity.QAlgorithm;
import com.ssafy.algogo.submission.entity.QSubmission;
import com.ssafy.algogo.submission.entity.QSubmissionAlgorithm;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.submission.repository.query.SubmissionQueryRepository;
import com.ssafy.algogo.user.dto.response.UserSimpleResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class SubmissionQueryRepositoryImpl implements SubmissionQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    QSubmission s = submission;
    QSubmissionAlgorithm sa = submissionAlgorithm;
    QAlgorithm a = algorithm;
    QProgramProblem pp = programProblem;
    QProblem p = problem;
    QProgram pg = program;
    QProgramType pt = programType;
    QReview r = review;

    LocalDateTime aWeekAgo = LocalDateTime.now().minusDays(7);

    @Override
    public Page<UserSubmissionResponseDto> findAllUserSubmissionList(Long userId,
        UserSubmissionRequestDto userSubmissionRequestDto,
        Pageable pageable) {

        List<UserSubmissionResponseDto> contents = jpaQueryFactory
            .from(s)
            .join(s.user, user)
            .join(s.programProblem, pp)
            .join(pp.problem, p)
            .join(pp.program, pg)
            .join(pg.programType, pt)
            .leftJoin(sa).on(sa.submission.eq(s))
            .leftJoin(sa.algorithm, a)
            .where(
                s.user.id.eq(userId),
                findUserSubmissionsDynamicConditions(userSubmissionRequestDto)
            )
            .orderBy(getSubmissionOrderSpecifiers(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .transform(
                groupBy(s.id).list(
                    Projections.constructor(
                        UserSubmissionResponseDto.class,
                        // User 정보 약식 부분
                        Projections.constructor(
                            UserSimpleResponseDto.class,
                            user.id,
                            user.profileImage,
                            user.nickname
                        ),
                        // submission 부분
                        Projections.constructor(
                            SubmissionResponseDto.class,
                            s.id,
                            s.programProblem.id,
                            s.user.id,
                            s.language,
                            s.code,
                            s.strategy,
                            s.execTime,
                            s.memory,
                            s.isSuccess,
                            s.viewCount,
                            s.createdAt,
                            s.modifiedAt,
                            // 알고리즘 부분
                            list(Projections.constructor(
                                AlgorithmResponseDto.class,
                                a.id,
                                a.name
                            )),
                            s.aiScore,
                            s.aiScoreReason
                        ),
                        // programProblem 부분
                        Projections.constructor(
                            ProgramProblemDetailResponseDto.class,
                            pp.id,
                            pp.participantCount,
                            pp.submissionCount,
                            pp.solvedCount,
                            pp.viewCount,
                            pp.startDate,
                            pp.endDate,
                            pp.userDifficultyType,
                            pp.difficultyViewType,
                            Projections.constructor(
                                ProblemResponseDto.class,
                                p.id,
                                p.platformType,
                                p.problemNo,
                                p.title,
                                p.difficultyType,
                                p.problemLink
                            )
                        ),
                        // Program 부분
                        Projections.constructor(
                            ProgramResponseDto.class,
                            pg.id,
                            pg.title,
                            pg.thumbnail,
                            Projections.constructor(
                                ProgramTypeResponseDto.class,
                                pt.id,
                                pt.name
                            )
                        )
                    )
                )
            );

        long totalCount = jpaQueryFactory
            .select(s.count())
            .from(s)
            .where(
                s.user.id.eq(userId),
                findUserSubmissionsDynamicConditions(userSubmissionRequestDto))
            .fetchOne();

        return new PageImpl<>(contents, pageable, totalCount);
    }

    @Override
    public List<ReviewCandidateQueryDto> findReviewMatchCandidates(Long subjectSubmissionId,
        Long subjectUserId,
        Long programProblemId,
        String language) {

        QRequireReview rr = requireReview;
        QReview rSub = new QReview("rSub");
        QRequireReview rrSub = new QRequireReview("rrSub");

        Map<Submission, ReviewCandidateQueryDto> result = jpaQueryFactory
            .from(s)
            .leftJoin(sa).on(sa.submission.eq(s))
            .leftJoin(r).on(r.submission.eq(s),
                r.parentReview.isNull())
            .leftJoin(rr).on(rr.targetSubmission.eq(s))
            .where(
                s.programProblem.id.eq(programProblemId),
                s.language.eq(language),
                s.user.id.ne(subjectUserId),
                s.id.ne(subjectSubmissionId),

                // 이미 리뷰를 단 제출건은 제외
                JPAExpressions
                    .selectOne()
                    .from(rSub)
                    .where(
                        rSub.submission.eq(s),
                        rSub.user.id.eq(subjectUserId),
                        rSub.parentReview.isNull()
                    )
                    .notExists(),

                // 이미 매칭된 매칭은 제외
                JPAExpressions
                    .selectOne()
                    .from(rrSub)
                    .where(
                        rrSub.subjectUser.id.eq(subjectUserId),
                        rrSub.targetSubmission.eq(s)
                    )
                    .notExists()

            )
            .transform(
                groupBy(s).as(
                    Projections.constructor(
                        ReviewCandidateQueryDto.class,
                        s,
                        list(sa.algorithm.id),
                        r.id.count(),
                        rr.id.count()
                    )
                )
            );
        return result.entrySet().stream()
            .filter(
                e -> e.getKey() != null)
            .map(
                e -> new ReviewCandidateQueryDto(
                    e.getKey(),
                    e.getValue().algorithmIdList(),
                    e.getValue().reviewCount(),
                    e.getValue().requireReviewCount())
            ).toList();
    }

    @Override
    public List<Long> findHotSubmissionIds() {
        return jpaQueryFactory
            .select(s.id)
            .from(s)
            .join(s.programProblem, pp)
            .join(pp.program, pg)
            .join(pg.programType, pt)
            .leftJoin(r).on(s.id.eq(r.submission.id))
            .where(
                s.createdAt.between(aWeekAgo, LocalDateTime.now()),
                pt.name.lower().ne("group"),
                pt.name.lower().ne("campaign")
                    .or(
                        pt.name.lower().eq("campaign")
                            .and(pp.startDate.loe(LocalDateTime.now()))
                            .and(pp.endDate.goe(LocalDateTime.now()))
                    )
            )
            .groupBy(s.id)
            .limit(10)
            .orderBy(
                r.id.count().desc(),
                s.createdAt.max().desc(),
                s.viewCount.max().desc())
            .fetch();
    }

    @Override
    public List<Long> findRecentSubmissionIds() {
        return jpaQueryFactory
            .select(s.id)
            .from(s)
            .join(s.programProblem, pp)
            .join(pp.program, pg)
            .join(pg.programType, pt)
            .leftJoin(r).on(s.id.eq(r.submission.id))
            .where(
                s.createdAt.between(aWeekAgo, LocalDateTime.now()),
                pt.name.lower().ne("group"),
                pt.name.lower().ne("campaign")
                    .or(
                        pt.name.lower().eq("campaign")
                            .and(pp.startDate.loe(LocalDateTime.now()))
                            .and(pp.endDate.goe(LocalDateTime.now()))
                    )
            )
            .groupBy(s.id)
            .limit(10)
            .orderBy(
                r.createdAt.max().desc(),
                s.createdAt.max().desc())
            .fetch();
    }

    @Override
    public List<Long> findTrendProgramProblemIds() {
        return jpaQueryFactory
            .select(pp.id)
            .from(s)
            .join(s.programProblem, pp)
            .join(pp.program, pg)
            .join(pg.programType, pt)
            .where(
                s.createdAt.between(aWeekAgo, LocalDateTime.now()),
                pt.name.lower().eq("problemset")
            )
            .groupBy(pp.id)
            .having(s.id.count().loe(3))
            .limit(10)
            .orderBy(
                s.id.count().asc(),
                s.createdAt.max().asc(),
                pp.viewCount.max().asc())
            .fetch();
    }


    @Override
    public Page<UserSubmissionResponseDto> findAllSubmissionsByProgramProblem(Long programProblemId,
        UserSubmissionRequestDto userSubmissionRequestDto,
        Pageable pageable) {

        List<UserSubmissionResponseDto> contents = jpaQueryFactory
            .from(s)
            .join(s.user, user)
            .join(s.programProblem, pp)
            .join(pp.problem, p)
            .join(pp.program, pg)
            .join(pg.programType, pt)
            .leftJoin(sa).on(sa.submission.eq(s))
            .leftJoin(sa.algorithm, a)
            .where(
                pp.id.eq(programProblemId),
                findUserSubmissionsDynamicConditions(userSubmissionRequestDto)
            )
            .orderBy(getSubmissionOrderSpecifiers(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .transform(
                groupBy(s.id).list(
                    Projections.constructor(
                        UserSubmissionResponseDto.class,
                        // User 정보 약식 부분
                        Projections.constructor(
                            UserSimpleResponseDto.class,
                            user.id,
                            user.profileImage,
                            user.nickname
                        ),
                        // submission 부분
                        Projections.constructor(
                            SubmissionResponseDto.class,
                            s.id,
                            s.programProblem.id,
                            s.user.id,
                            s.language,
                            s.code,
                            s.strategy,
                            s.execTime,
                            s.memory,
                            s.isSuccess,
                            s.viewCount,
                            s.createdAt,
                            s.modifiedAt,
                            // 알고리즘 부분
                            list(Projections.constructor(
                                AlgorithmResponseDto.class,
                                a.id,
                                a.name
                            )),
                            s.aiScore,
                            s.aiScoreReason
                        ),
                        // programProblem 부분
                        Projections.constructor(
                            ProgramProblemDetailResponseDto.class,
                            pp.id,
                            pp.participantCount,
                            pp.submissionCount,
                            pp.solvedCount,
                            pp.viewCount,
                            pp.startDate,
                            pp.endDate,
                            pp.userDifficultyType,
                            pp.difficultyViewType,
                            Projections.constructor(
                                ProblemResponseDto.class,
                                p.id,
                                p.platformType,
                                p.problemNo,
                                p.title,
                                p.difficultyType,
                                p.problemLink
                            )
                        ),
                        // Program 부분
                        Projections.constructor(
                            ProgramResponseDto.class,
                            pg.id,
                            pg.title,
                            pg.thumbnail,
                            Projections.constructor(
                                ProgramTypeResponseDto.class,
                                pt.id,
                                pt.name
                            )
                        )
                    )
                )
            );

        long totalCount = jpaQueryFactory
            .select(s.count())
            .from(s)
            .where(
                pp.id.eq(programProblemId),
                findUserSubmissionsDynamicConditions(userSubmissionRequestDto))
            .fetchOne();

        return new PageImpl<>(contents, pageable, totalCount);
    }

    private Predicate findUserSubmissionsDynamicConditions(
        UserSubmissionRequestDto userSubmissionRequestDto) {
        return ExpressionUtils.allOf(
            eqAlgorithm(userSubmissionRequestDto.getAlgorithm()),
            eqIsSuccess(userSubmissionRequestDto.getIsSuccess()),
            eqLanguage(userSubmissionRequestDto.getLanguage()),
            eqPlatform(userSubmissionRequestDto.getPlatform()),
            eqProgramType(userSubmissionRequestDto.getProgramType())
        );
    }

    @Override
    public Boolean isSubmissionAuthorActive(Long submissionId) {
        return jpaQueryFactory
            .select(
                programUser.programUserStatus.eq(ProgramUserStatus.ACTIVE)
            )
            .from(s)
            .join(s.programProblem, pp)
            .join(pp.program, pg)
            .join(programUser)
            .on(
                programUser.user.id.eq(s.user.id)
                    .and(programUser.program.id.eq(pg.id))
            )
            .where(s.id.eq(submissionId))
            .fetchOne();
    }

    private BooleanExpression eqLanguage(String language) {
        return (StringUtils.hasText(language)) ? submission.language.eq(language) : null;
    }

    private BooleanExpression eqIsSuccess(Boolean isSuccess) {
        return (isSuccess != null) ? submission.isSuccess.eq(isSuccess) : null;
    }

    private BooleanExpression eqProgramType(String programType) {
        return (StringUtils.hasText(programType)) ? program.programType.name.eq(
            programType.toLowerCase().trim()) : null;
    }

    private BooleanExpression eqAlgorithm(String usedAlgorithm) {
        return (StringUtils.hasText(usedAlgorithm)) ? algorithm.name.eq(usedAlgorithm) : null;
    }

    private BooleanExpression eqPlatform(String platform) {
        return (StringUtils.hasText(platform)) ? problem.platformType.eq(
            PlatformType.valueOf(platform.trim().toUpperCase())) : null;
    }

    private OrderSpecifier<?> getSubmissionOrderSpecifiers(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return null;
        }
        Sort.Order order = pageable.getSort().iterator().next();
        Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

        return switch (order.getProperty()) {
            case "execTime" -> new OrderSpecifier<>(direction, submission.execTime);
            case "memory" -> new OrderSpecifier<>(direction, submission.memory);
            default -> new OrderSpecifier<>(direction, submission.modifiedAt);
        };
    }
}
