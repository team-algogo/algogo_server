package com.ssafy.algogo.program.repository.query;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.program.entity.QProgramCategory.programCategory;
import static com.ssafy.algogo.program.entity.QProgramType.programType;
import static com.ssafy.algogo.program.entity.QCategory.category;
import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.problem.entity.QProblem.problem;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ProgramQueryRepositoryImpl implements ProgramQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<ProblemSetResponseDto> findProblemSetWithCategoriesAndPopularity(
		String keyword,
		String categoryName,
		String sortBy,
		String sortDirection,
		int size,
		int page
	) {
		BooleanExpression isProblemSet =
			program.programType.name.eq("problemset");

		BooleanExpression titleContains =
			StringUtils.hasText(keyword)
				? program.title.containsIgnoreCase(keyword.trim())
				: null;

		BooleanExpression categoryEq =
			StringUtils.hasText(categoryName)
				? category.name.eq(categoryName.trim())
				: null;

		// 총 참여자 수
		NumberExpression<Long> popularityScore =
			programProblem.participantCount.sum().coalesce(0L);

		// 프로그램별 문제 개수 (중복 방지하려면 countDistinct)
		NumberExpression<Long> problemCountExpr =
			programProblem.id.countDistinct().coalesce(0L);

		Order direction =
			"asc".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

		OrderSpecifier<?> orderSpecifier = switch (sortBy == null ? "" : sortBy) {
			case "popular" -> new OrderSpecifier<>(direction, popularityScore);
			case "createdAt", "created_at", "" ->
				new OrderSpecifier<>(direction, program.createdAt);
			default -> new OrderSpecifier<>(direction, program.createdAt);
		};

		return queryFactory
			.from(program)
			.join(program.programType, programType)
			.leftJoin(programProblem).on(programProblem.program.eq(program))
			.leftJoin(programCategory).on(programCategory.program.eq(program))
			.leftJoin(programCategory.category, category)
			.where(isProblemSet, titleContains, categoryEq)
			.groupBy(program.id, programType.id)
			.orderBy(orderSpecifier)
			.offset((long) page * size)
			.limit(size)
			.transform(
				groupBy(program.id).list(
					Projections.constructor(
						ProblemSetResponseDto.class,
						program.id,
						program.title,
						program.description,
						program.thumbnail,
						program.createdAt,
						program.modifiedAt,
						programType.name,
						list(category.name),
						popularityScore,    // totalParticipants
						problemCountExpr    // problemCount (record 마지막 필드)
					)
				)
			);
	}


	@Override
	public long countProblemSetWithFilter(String keyword, String categoryName) {
		BooleanExpression isProblemSet =
			program.programType.name.eq("problemset");

		BooleanExpression titleContains =
			StringUtils.hasText(keyword)
				? program.title.containsIgnoreCase(keyword.trim())
				: null;

		BooleanExpression categoryEq =
			StringUtils.hasText(categoryName)
				? category.name.eq(categoryName.trim())
				: null;

		Long count = queryFactory
			.select(program.id.countDistinct())
			.from(program)
			.join(program.programType, programType)
			.leftJoin(programCategory).on(programCategory.program.eq(program))
			.leftJoin(programCategory.category, category)
			.where(isProblemSet, titleContains, categoryEq)
			.fetchOne();

		return count != null ? count : 0L;
	}

	@Override
	public ProblemSetResponseDto findProblemSetDetail(Long programId) {

		NumberExpression<Long> popularityScore =
			programProblem.participantCount.sum().coalesce(0L);

		NumberExpression<Long> problemCountExpr =
			programProblem.id.countDistinct().coalesce(0L);

		return queryFactory
			.from(program)
			.join(program.programType, programType)
			.leftJoin(programProblem).on(programProblem.program.eq(program))
			.leftJoin(programCategory).on(programCategory.program.eq(program))
			.leftJoin(programCategory.category, category)
			.where(
				program.id.eq(programId),
				program.programType.name.eq("problemset")
			)
			.groupBy(program.id, programType.id)
			.transform(
				groupBy(program.id).list(
					Projections.constructor(
						ProblemSetResponseDto.class,
						program.id,
						program.title,
						program.description,
						program.thumbnail,
						program.createdAt,
						program.modifiedAt,
						programType.name,
						list(category.name),
						popularityScore,
						problemCountExpr
					)
				)
			)
			.stream()
			.findFirst()
			.orElse(null);
	}

	@Override
	public List<ProblemSetResponseDto> searchProblemSetByKeyword(String keyword) {
		// 띄어쓰기 제거 (Java)
		String normalizedKeyword = keyword.replaceAll("\\s+", "");

		NumberExpression<Long> popularityScore =
			programProblem.participantCount.sum().coalesce(0L);

		NumberExpression<Long> problemCountExpr =
			programProblem.id.countDistinct().coalesce(0L);

		return queryFactory
			.from(program)
			.join(program.programType, programType)
			.leftJoin(programProblem).on(programProblem.program.eq(program))
			.leftJoin(programProblem.problem, problem)
			.leftJoin(programCategory).on(programCategory.program.eq(program))
			.leftJoin(programCategory.category, category)
			.where(
				program.programType.name.eq("problemset"),
				// DB에서도 띄어쓰기 제거
				Expressions.stringTemplate(
						"REPLACE(LOWER({0}), ' ', '')",
						program.title
					).contains(normalizedKeyword.toLowerCase())
					.or(Expressions.stringTemplate(
						"REPLACE(LOWER({0}), ' ', '')",
						program.description
					).contains(normalizedKeyword.toLowerCase()))
					.or(Expressions.stringTemplate(
						"REPLACE(LOWER({0}), ' ', '')",
						problem.title
					).contains(normalizedKeyword.toLowerCase()))
			)
			.groupBy(program.id, programType.id)
			.transform(
				groupBy(program.id).list(
					Projections.constructor(
						ProblemSetResponseDto.class,
						program.id,
						program.title,
						program.description,
						program.thumbnail,
						program.createdAt,
						program.modifiedAt,
						programType.name,
						list(category.name),
						popularityScore,
						problemCountExpr
					)
				)
			);
	}



	private boolean hasText(String value) {
		return StringUtils.hasText(value);
	}
}
