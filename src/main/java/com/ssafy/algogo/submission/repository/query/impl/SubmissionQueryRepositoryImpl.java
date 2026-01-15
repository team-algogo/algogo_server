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
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.problem.dto.response.ProblemResponseDto;
import com.ssafy.algogo.problem.entity.PlatformType;
import com.ssafy.algogo.problem.entity.QProblem;
import com.ssafy.algogo.problem.entity.QProgramProblem;
import com.ssafy.algogo.program.dto.response.ProgramResponseDto;
import com.ssafy.algogo.program.dto.response.ProgramTypeResponseDto;
import com.ssafy.algogo.program.entity.QProgram;
import com.ssafy.algogo.program.entity.QProgramType;
import com.ssafy.algogo.program.entity.QProgramUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.review.entity.QRequireReview;
import com.ssafy.algogo.review.entity.QReview;
import com.ssafy.algogo.submission.dto.ReviewCandidateQueryDto;
import com.ssafy.algogo.submission.dto.request.UserSubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.AlgorithmResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionMeResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionStatsResponseDto;
import com.ssafy.algogo.submission.entity.QAlgorithm;
import com.ssafy.algogo.submission.entity.QSubmission;
import com.ssafy.algogo.submission.entity.QSubmissionAlgorithm;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.submission.repository.query.SubmissionQueryRepository;
import com.ssafy.algogo.user.dto.response.UserSimpleResponseDto;
import com.ssafy.algogo.user.entity.QUser;
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
    QUser u = user;

    LocalDateTime aWeekAgo = LocalDateTime.now().minusDays(7);

    @Override
    public Page<SubmissionMeResponseDto> findAllUserSubmissionList(
        Long userId,
        UserSubmissionRequestDto userSubmissionRequestDto,
        Pageable pageable
    ) {
        // 총 개수 확인
        JPQLQuery<Long> preTotalCountQuery = jpaQueryFactory
            .select(s.id.countDistinct())
            .from(s)
            .where(
                s.user.id.eq(userId),
                findUserSubmissionsDynamicConditions(userSubmissionRequestDto)
            );

        // 동적 조인
        applyDynamicJoins(preTotalCountQuery, userSubmissionRequestDto);
        Long preTotalCount = preTotalCountQuery.fetchOne();

        long totalCount = (preTotalCount == null) ? 0L : preTotalCount;

        // totalCount가 0이면 내용 없음
        if (totalCount == 0L) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        // submissionId만 먼저 조회
        JPQLQuery<Long> submissionIdsQuery = jpaQueryFactory
            .select(s.id).distinct()
            .from(s)
            .where(
                submission.user.id.eq(userId),
                findUserSubmissionsDynamicConditions(userSubmissionRequestDto)
            )
            .orderBy(getSubmissionOrderSpecifiers(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        applyDynamicJoins(submissionIdsQuery, userSubmissionRequestDto);
        List<Long> submissionIds = submissionIdsQuery.fetch();

        if (submissionIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, totalCount);
        }

        // reviewCount용
        QReview r2 = new QReview("r2");

        // 찾은 id들의 전체 내용 조회
        List<SubmissionMeResponseDto> contents = jpaQueryFactory
            .from(s)
            .join(s.user, user)
            .join(s.programProblem, pp)
            .join(pp.problem, p)
            .join(pp.program, pg)
            .join(pg.programType, pt)
            .leftJoin(sa).on(sa.submission.eq(s))
            .leftJoin(sa.algorithm, a)
            .where(s.id.in(submissionIds))
            .orderBy(getSubmissionOrderSpecifiers(pageable))
            .transform(
                groupBy(s.id).list(
                    Projections.constructor(
                        SubmissionMeResponseDto.class,
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
                            // algorithm 부분
                            list(
                                Projections.constructor(
                                    AlgorithmResponseDto.class,
                                    a.id,
                                    a.name
                                )
                            ),
                            s.aiScore,
                            s.aiScoreReason
                        ),
                        // 해당 submission의 리뷰 개수
                        JPAExpressions
                            .select(r2.id.count())
                            .from(r2)
                            .where(r2.submission.id.eq(s.id)
                            ),
                        // problem 부분
                        Projections.constructor(
                            ProblemResponseDto.class,
                            p.id,
                            p.platformType,
                            p.problemNo,
                            p.title,
                            p.difficultyType,
                            p.problemLink
                        ),
                        // program 부분
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
        QProgramUser pu = QProgramUser.programUser;

        Map<Submission, ReviewCandidateQueryDto> result = jpaQueryFactory
            .from(s)
            .innerJoin(s.programProblem, pp)
            .innerJoin(pp.program, pg)
            .innerJoin(pg.programType, pt)
            .leftJoin(pu).on(
                pu.user.id.eq(s.user.id),
                pu.program.id.eq(p.id)
            )
            .leftJoin(sa).on(sa.submission.eq(s))
            .leftJoin(r).on(r.submission.eq(s),
                r.parentReview.isNull())
            .leftJoin(rr).on(rr.targetSubmission.eq(s))
            .where(
                s.programProblem.id.eq(programProblemId),
                s.language.eq(language),
                s.user.id.ne(subjectUserId),
                s.id.ne(subjectSubmissionId),

                // programs_users에 사용자 상태가 ACTIVE인 경우만 필터링
                pt.name.eq("PROBLEMSET")
                    .or(pu.programUserStatus.eq(ProgramUserStatus.ACTIVE)),
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
    public Page<SubmissionStatsResponseDto> findAllSubmissionsByProgramProblem(
        Long programProblemId,
        UserSubmissionRequestDto userSubmissionRequestDto,
        Pageable pageable) {

        JPQLQuery<Long> preCountQuery = jpaQueryFactory
            .select(s.id.countDistinct())
            .from(s)
            .where(
                s.programProblem.id.eq(programProblemId),
                findUserSubmissionsDynamicConditions(userSubmissionRequestDto)
            );

        // 동적 조인
        applyDynamicJoins(preCountQuery, userSubmissionRequestDto);
        Long preTotalCount = preCountQuery.fetchOne();

        long totalCount = (preTotalCount == null) ? 0L : preTotalCount;

        if (totalCount == 0L) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        JPQLQuery<Long> submissionIdsQuery = jpaQueryFactory
            .select(s.id)
            .from(s)
            .where(
                s.programProblem.id.eq(programProblemId),
                findUserSubmissionsDynamicConditions(userSubmissionRequestDto)
            )
            .distinct()
            .orderBy(getSubmissionOrderSpecifiers(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        // 동적 조인
        applyDynamicJoins(submissionIdsQuery, userSubmissionRequestDto);
        List<Long> submissionIds = submissionIdsQuery.fetch();

        if (submissionIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, totalCount);
        }

        QReview r2 = new QReview("r2");
        List<SubmissionStatsResponseDto> contents = jpaQueryFactory
            .from(s)
            .join(s.user, u)
            .join(s.programProblem, pp)
            .join(pp.problem, p)
            .join(pp.program, pg)
            .join(pg.programType, pt)
            .leftJoin(sa).on(sa.submission.eq(s))
            .leftJoin(sa.algorithm, a)
            .where(s.id.in(submissionIds))
            .orderBy(getSubmissionOrderSpecifiers(pageable))
            .transform(
                groupBy(s.id).list(
                    Projections.constructor(
                        SubmissionStatsResponseDto.class,
                        // User 부분
                        Projections.constructor(
                            UserSimpleResponseDto.class,
                            u.id,
                            u.profileImage,
                            u.nickname
                        ),
                        // Submission 부분
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
                            list(
                                Projections.constructor(
                                    AlgorithmResponseDto.class,
                                    a.id,
                                    a.name
                                )
                            ),
                            s.aiScore,
                            s.aiScoreReason
                        ),
                        // 리뷰 개수 부분
                        JPAExpressions
                            .select(r2.id.count())
                            .from(r2)
                            .where(r2.submission.id.eq(s.id))
                    )
                )
            );
        return new PageImpl<>(contents, pageable, totalCount);
    }

    @Override
    public boolean canUserMoreSubmit(Long userId, String inputProgramType, Long programId) {
        QRequireReview rr = requireReview;

        Long count = jpaQueryFactory
            .select(rr.count())
            .from(rr)
            // submission -> programs_problems -> programs -> program_types 순으로 조인
            .join(rr.subjectSubmission, submission)
            .join(submission.programProblem, programProblem)
            .join(programProblem.program, program)
            .join(programType).on(program.programType.id.eq(programType.id))
            .where(
                rr.subjectUser.id.eq(userId),
                rr.isDone.isFalse(),
                programType.name.eq(inputProgramType),
                // 그룹일 경우에만 programId로 카운트 (문제집은 전체 대상이므로 조건 제외)
                program.id.eq(programId)
            )
            .fetchOne();

        return count != null && count == 0;
    }

    private Predicate findUserSubmissionsDynamicConditions(
        UserSubmissionRequestDto userSubmissionRequestDto) {
        return ExpressionUtils.allOf(
            eqAlgorithm(userSubmissionRequestDto.getAlgorithm()),
            eqIsSuccess(userSubmissionRequestDto.getIsSuccess()),
            eqLanguage(userSubmissionRequestDto.getLanguage()),
            eqPlatform(userSubmissionRequestDto.getPlatform()),
            eqProgramType(userSubmissionRequestDto.getProgramType()),
            eqNickname(userSubmissionRequestDto.getNickname())
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

    private void applyDynamicJoins(JPQLQuery<?> query, UserSubmissionRequestDto dto) {
        query.join(s.programProblem, pp);

        if (StringUtils.hasText(dto.getNickname())) {
            query.join(s.user, u);
        }

        if (StringUtils.hasText(dto.getProgramType())) {
            query.join(pp.program, pg)
                .join(pg.programType, pt);
        }

        if (StringUtils.hasText(dto.getPlatform())) {
            query.join(pp.problem, p);
        }

        if (StringUtils.hasText(dto.getAlgorithm())) {
            query.leftJoin(sa).on(sa.submission.eq(s))
                .leftJoin(sa.algorithm, a);
        }
    }

    private BooleanExpression eqLanguage(String language) {
        return (StringUtils.hasText(language)) ?
            submission.language.toLowerCase().eq(language.toLowerCase().trim())
            : null;
    }

    private BooleanExpression eqIsSuccess(Boolean isSuccess) {
        return (isSuccess != null) ? submission.isSuccess.eq(isSuccess) : null;
    }

    private BooleanExpression eqProgramType(String programType) {
        return (StringUtils.hasText(programType)) ? pt.name.eq(
            programType.toLowerCase().trim())
            : null;
    }

    private BooleanExpression eqAlgorithm(String usedAlgorithm) {
        return (StringUtils.hasText(usedAlgorithm)) ? algorithm.name.eq(usedAlgorithm)
            : null;
    }

    private BooleanExpression eqPlatform(String platform) {
        return (StringUtils.hasText(platform)) ?
            problem.platformType.eq(PlatformType.valueOf(platform.trim().toUpperCase()))
            : null;
    }

    private BooleanExpression eqNickname(String nickname) {
        return (StringUtils.hasText(nickname)) ? u.nickname.contains(nickname)
            : null;
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
            default -> new OrderSpecifier<>(direction, submission.createdAt);
        };
    }
}
