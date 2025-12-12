package com.ssafy.algogo.program.repository.query;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.program.entity.QProgramCategory.programCategory;
import static com.ssafy.algogo.program.entity.QProgramType.programType;
import static com.ssafy.algogo.program.entity.QCategory.category;
import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
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
		String sortDirection
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

		// 총 참여자 수 = 해당 프로그램의 모든 ProgramProblem.participant_count 합
		NumberExpression<Long> popularityScore =
			programProblem.participantCount.sum().coalesce(0L);

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
			// Program ↔ ProgramProblem (직접 on 조인)
			.leftJoin(programProblem)
			.on(programProblem.program.eq(program))
			// Program ↔ ProgramCategory ↔ Category (역시 on 조인)
			.leftJoin(programCategory)
			.on(programCategory.program.eq(program))
			.leftJoin(programCategory.category, category)
			.where(isProblemSet, titleContains, categoryEq)
			.groupBy(program.id, programType.id)
			.orderBy(orderSpecifier)
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
						popularityScore       // totalParticipants
					)
				)
			);
	}


	private boolean hasText(String value) {
		return StringUtils.hasText(value);
	}
}
