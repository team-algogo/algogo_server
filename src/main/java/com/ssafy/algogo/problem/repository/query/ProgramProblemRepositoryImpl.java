package com.ssafy.algogo.problem.repository.query;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.problem.dto.response.ProgramProblemResponseDto;
import com.ssafy.algogo.problem.entity.DifficultyType;
import com.ssafy.algogo.problem.entity.QProblem;
import com.ssafy.algogo.problem.entity.QProgramProblem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class ProgramProblemRepositoryImpl implements ProgramProblemRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<ProgramProblemResponseDto> findAllByProgramIdWithSort(
		Long programId,
		String sortBy,
		String sortDirection,
		Pageable pageable
	) {
		QProgramProblem pp = QProgramProblem.programProblem;
		QProblem p = QProblem.problem;

		// 난이도 score 표현: DifficultyType → score 값
		NumberExpression<Integer> difficultyScoreExpr = new CaseBuilder()
			.when(p.difficultyType.eq(DifficultyType.UNRATED)).then(0)

			.when(p.difficultyType.eq(DifficultyType.BRONZE_5)).then(5)
			.when(p.difficultyType.eq(DifficultyType.BRONZE_4)).then(6)
			.when(p.difficultyType.eq(DifficultyType.BRONZE_3)).then(7)
			.when(p.difficultyType.eq(DifficultyType.BRONZE_2)).then(8)
			.when(p.difficultyType.eq(DifficultyType.BRONZE_1)).then(9)

			.when(p.difficultyType.eq(DifficultyType.SILVER_5)).then(10)
			.when(p.difficultyType.eq(DifficultyType.SILVER_4)).then(11)
			.when(p.difficultyType.eq(DifficultyType.SILVER_3)).then(12)
			.when(p.difficultyType.eq(DifficultyType.SILVER_2)).then(13)
			.when(p.difficultyType.eq(DifficultyType.SILVER_1)).then(14)

			.when(p.difficultyType.eq(DifficultyType.GOLD_5)).then(15)
			.when(p.difficultyType.eq(DifficultyType.GOLD_4)).then(16)
			.when(p.difficultyType.eq(DifficultyType.GOLD_3)).then(17)
			.when(p.difficultyType.eq(DifficultyType.GOLD_2)).then(18)
			.when(p.difficultyType.eq(DifficultyType.GOLD_1)).then(19)

			.when(p.difficultyType.eq(DifficultyType.PLATINUM_5)).then(20)
			.when(p.difficultyType.eq(DifficultyType.PLATINUM_4)).then(21)
			.when(p.difficultyType.eq(DifficultyType.PLATINUM_3)).then(22)
			.when(p.difficultyType.eq(DifficultyType.PLATINUM_2)).then(23)
			.when(p.difficultyType.eq(DifficultyType.PLATINUM_1)).then(24)

			.when(p.difficultyType.eq(DifficultyType.DIAMOND_5)).then(25)
			.when(p.difficultyType.eq(DifficultyType.DIAMOND_4)).then(26)
			.when(p.difficultyType.eq(DifficultyType.DIAMOND_3)).then(27)
			.when(p.difficultyType.eq(DifficultyType.DIAMOND_2)).then(28)
			.when(p.difficultyType.eq(DifficultyType.DIAMOND_1)).then(29)

			.when(p.difficultyType.eq(DifficultyType.RUBY_5)).then(30)
			.when(p.difficultyType.eq(DifficultyType.RUBY_4)).then(31)
			.when(p.difficultyType.eq(DifficultyType.RUBY_3)).then(32)
			.when(p.difficultyType.eq(DifficultyType.RUBY_2)).then(33)
			.when(p.difficultyType.eq(DifficultyType.RUBY_1)).then(34)

			.when(p.difficultyType.eq(DifficultyType.LEVEL_0)).then(3)
			.when(p.difficultyType.eq(DifficultyType.LEVEL_1)).then(7)
			.when(p.difficultyType.eq(DifficultyType.LEVEL_2)).then(12)
			.when(p.difficultyType.eq(DifficultyType.LEVEL_3)).then(17)
			.when(p.difficultyType.eq(DifficultyType.LEVEL_4)).then(22)
			.when(p.difficultyType.eq(DifficultyType.LEVEL_5)).then(27)

			.when(p.difficultyType.eq(DifficultyType.D1)).then(5)
			.when(p.difficultyType.eq(DifficultyType.D2)).then(8)
			.when(p.difficultyType.eq(DifficultyType.D3)).then(12)
			.when(p.difficultyType.eq(DifficultyType.D4)).then(16)
			.when(p.difficultyType.eq(DifficultyType.D5)).then(20)
			.when(p.difficultyType.eq(DifficultyType.D6)).then(24)
			.when(p.difficultyType.eq(DifficultyType.D7)).then(28)
			.when(p.difficultyType.eq(DifficultyType.D8)).then(32)
			.otherwise(0);

		Order order = "desc".equalsIgnoreCase(sortDirection) ? Order.DESC : Order.ASC;
		OrderSpecifier<?> orderSpecifier = switch (sortBy) {
			case "difficulty" -> new OrderSpecifier<>(order, difficultyScoreExpr);
			case "participantCount" -> new OrderSpecifier<>(order, pp.participantCount);
			case "submissionCount" -> new OrderSpecifier<>(order, pp.submissionCount);
			case "solvedCount" -> new OrderSpecifier<>(order, pp.solvedCount);
			case "viewCount" -> new OrderSpecifier<>(order, pp.viewCount);
			case "startDate" -> new OrderSpecifier<>(order, pp.startDate);
			case "endDate" -> new OrderSpecifier<>(order, pp.endDate);
			case "id" -> new OrderSpecifier<>(order, pp.id);
			default -> new OrderSpecifier<>(order, pp.id);
		};

		// content 조회
		List<ProgramProblemResponseDto> content = queryFactory
			.select(
				com.querydsl.core.types.Projections.constructor(
					ProgramProblemResponseDto.class,
					pp.id,                      // programProblemId
					pp.participantCount,
					pp.submissionCount,
					pp.solvedCount,
					pp.viewCount,
					pp.startDate,
					pp.endDate,
					pp.userDifficultyType,
					pp.difficultyViewType,
					// 난이도 score
					difficultyScoreExpr,
					// 정답률 계산 (제출 0이면 null)
					new CaseBuilder()
						.when(pp.submissionCount.gt(0L))
						.then(pp.solvedCount.doubleValue()
							.multiply(100.0)
							.divide(pp.submissionCount.doubleValue()))
						.otherwise((Double) null),
					// ★ ProblemResponseDto projection (파라미터 6개, 순서/타입 정확히 맞춤)
					com.querydsl.core.types.Projections.constructor(
						com.ssafy.algogo.problem.dto.response.ProblemResponseDto.class,
						p.id,             // Long id
						p.platformType,   // PlatformType platformType
						p.problemNo,      // String problemNo
						p.title,          // String title
						p.difficultyType, // DifficultyType difficultyType
						p.problemLink     // String problemLink
					)
				)
			)
			.from(pp)
			.join(pp.problem, p)
			.where(pp.program.id.eq(programId))
			.orderBy(orderSpecifier)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// total count
		Long total = queryFactory
			.select(pp.count())
			.from(pp)
			.where(pp.program.id.eq(programId))
			.fetchOne();

		return PageableExecutionUtils.getPage(content, pageable, () -> total == null ? 0L : total);
	}
}
