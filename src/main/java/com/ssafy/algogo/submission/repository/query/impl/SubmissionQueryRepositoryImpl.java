package com.ssafy.algogo.submission.repository.query.impl;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.Projections.list;
import static com.ssafy.algogo.problem.entity.QProblem.problem;
import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.program.entity.QProgramType.programType;
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
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.problem.dto.response.ProblemResponseDto;
import com.ssafy.algogo.problem.dto.response.ProgramProblemResponseDto;
import com.ssafy.algogo.problem.entity.PlatformType;
import com.ssafy.algogo.problem.entity.QProblem;
import com.ssafy.algogo.problem.entity.QProgramProblem;
import com.ssafy.algogo.program.dto.response.ProgramResponseDto;
import com.ssafy.algogo.program.dto.response.ProgramTypeResponseDto;
import com.ssafy.algogo.program.entity.QProgram;
import com.ssafy.algogo.program.entity.QProgramType;
import com.ssafy.algogo.review.entity.QReview;
import com.ssafy.algogo.submission.dto.request.UserSubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.AlgorithmResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;
import com.ssafy.algogo.submission.dto.response.UserSubmissionResponseDto;
import com.ssafy.algogo.submission.entity.QAlgorithm;
import com.ssafy.algogo.submission.entity.QSubmission;
import com.ssafy.algogo.submission.entity.QSubmissionAlgorithm;
import com.ssafy.algogo.submission.repository.query.SubmissionQueryRepository;
import com.ssafy.algogo.user.entity.QUser;
import java.time.LocalDateTime;
import java.util.List;
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
    QUser u = user;
    QReview r = review;

    LocalDateTime aWeekAgo = LocalDateTime.now().minusDays(7);

    @Override
    public Page<UserSubmissionResponseDto> findAllUserSubmissionList(Long userId,
        UserSubmissionRequestDto userSubmissionRequestDto,
        Pageable pageable) {

        List<UserSubmissionResponseDto> contents = jpaQueryFactory
            .from(s)
            .join(s.programProblem, pp)
            .join(pp.problem, p)
            .join(pp.program, pg)
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
                            ))
                        ),
                        // programProblem 부분
                        Projections.constructor(
                            ProgramProblemResponseDto.class,
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
            .where(findUserSubmissionsDynamicConditions(userSubmissionRequestDto))
            .fetchOne();

        return new PageImpl<>(contents, pageable, totalCount);
    }

//    @Override
//    public List<SubmissionPreviewResponseDto> findHottestSubmissions() {
//
//        return jpaQueryFactory
//            .from(s)
//            .join(s.user, u)
//            .join(s.programProblem, pp)
//            .join(pp.program, pg)
//            .join(pp.problem, p)
//            .leftJoin(pt).on(pg.programType.eq(pt))
//            .leftJoin(sa).on(sa.submission.eq(s))
//            .leftJoin(sa.algorithm, a)
//            .leftJoin(r).on(
//                r.submission.eq(s),
//                r.createdAt.between(aWeekAgo, LocalDateTime.now())
//            )
////            .where(pt.name.ne("GROUP"))
//            .orderBy(
//                r.id.count().desc(),
//                pp.submissionCount.desc()
//            )
//            .transform(
//                groupBy(p.id).list(
//                    Projections.constructor(
//                        SubmissionPreviewResponseDto.class,
//                        s.id,
//                        s.language,
//                        s.isSuccess,
//                        s.createdAt,
//
//                        u.nickname,
//
//                        pg.title,
//                        pg.thumbnail,
//
//                        pg.programType.name,
//
//                        pp.id,
//                        pp.submissionCount,
//                        pp.viewCount,
//
//                        p.platformType,
//                        p.problemNo,
//                        p.title,
//                        p.difficultyType,
//
//                        r.id.count(),
//
//                        list(
//                            Projections.constructor(
//                                AlgorithmResponseDto.class,
//                                a.id,
//                                a.name
//                            )
//
//                        )
//                    )
//                )
//            );
//    }


    public List<Long> findMostPopularProblemIds() {
        return jpaQueryFactory
            .select(p.id)
            .from(s)
            .join(s.programProblem, pp)
            .join(pp.problem, p)
            .join(pp.program, pg)
            .join(pg.programType, pt)
            .where(
                s.createdAt.between(aWeekAgo, LocalDateTime.now()),
                pt.name.ne("GROUP")
            )
            .groupBy(p.id)
            .orderBy(s.id.count().desc())
            .limit(10)
            .fetch();
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
