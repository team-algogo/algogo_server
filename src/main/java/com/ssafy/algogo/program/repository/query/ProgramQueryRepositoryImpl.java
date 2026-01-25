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
import com.ssafy.algogo.problem.entity.QProblem;
import com.ssafy.algogo.problem.entity.QProgramProblem;
import com.ssafy.algogo.program.entity.QProgram;
import com.ssafy.algogo.program.entity.QProgramCategory;
import com.ssafy.algogo.program.entity.QProgramType;
import com.ssafy.algogo.program.entity.QProgramUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.program.group.entity.QGroupsUser;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetWithMatchResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetWithProgressResponseDto;
import com.ssafy.algogo.submission.entity.QSubmission;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
			int page) {
		BooleanExpression isProblemSet = program.programType.name.eq("problemset");

		BooleanExpression titleContains = StringUtils.hasText(keyword)
				? program.title.containsIgnoreCase(keyword.trim())
				: null;

		BooleanExpression categoryEq = StringUtils.hasText(categoryName)
				? category.name.eq(categoryName.trim())
				: null;

		QSubmission submission = QSubmission.submission;

		// 총 참여자 수
		NumberExpression<Long> popularityScore = submission.user.id.countDistinct().coalesce(0L);

		// 프로그램별 문제 개수 (중복 방지하려면 countDistinct)
		NumberExpression<Long> problemCountExpr = programProblem.id.countDistinct().coalesce(0L);

		Order direction = "asc".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

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
				.leftJoin(submission).on(submission.programProblem.eq(programProblem))
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
										popularityScore, // totalParticipants
										problemCountExpr // problemCount
								)));
	}

	@Override
	public long countProblemSetWithFilter(String keyword, String categoryName) {
		BooleanExpression isProblemSet = program.programType.name.eq("problemset");

		BooleanExpression titleContains = StringUtils.hasText(keyword)
				? program.title.containsIgnoreCase(keyword.trim())
				: null;

		BooleanExpression categoryEq = StringUtils.hasText(categoryName)
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

		QSubmission submission = QSubmission.submission;

		NumberExpression<Long> popularityScore = submission.user.id.countDistinct().coalesce(0L);

		NumberExpression<Long> problemCountExpr = programProblem.id.countDistinct().coalesce(0L);

		return queryFactory
				.from(program)
				.join(program.programType, programType)
				.leftJoin(programProblem).on(programProblem.program.eq(program))
				.leftJoin(submission).on(submission.programProblem.eq(programProblem))
				.leftJoin(programCategory).on(programCategory.program.eq(program))
				.leftJoin(programCategory.category, category)
				.where(
						program.id.eq(programId),
						program.programType.name.eq("problemset"))
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
										problemCountExpr)))
				.stream()
				.findFirst()
				.orElse(null);
	}

	/**
	 * 문제집 제목/설명으로 검색 (페이지네이션)
	 */
	@Override
	public Page<ProblemSetResponseDto> searchProblemSetByTitleOrDescription(
			String keyword, Pageable pageable) {

		BooleanExpression isProblemSet = program.programType.name.eq("problemset");

		BooleanExpression keywordFilter = program.title.containsIgnoreCase(keyword)
				.or(program.description.containsIgnoreCase(keyword));

		// 총 개수 조회
		Long total = queryFactory
				.select(program.id.countDistinct())
				.from(program)
				.join(program.programType, programType)
				.where(isProblemSet, keywordFilter)
				.fetchOne();

		total = total != null ? total : 0L;

		QSubmission submission = QSubmission.submission;

		NumberExpression<Long> popularityScore = submission.user.id.countDistinct().coalesce(0L);

		NumberExpression<Long> problemCountExpr = programProblem.id.countDistinct().coalesce(0L);

		// 페이지네이션된 결과 조회
		List<ProblemSetResponseDto> content = queryFactory
				.from(program)
				.join(program.programType, programType)
				.leftJoin(programProblem).on(programProblem.program.eq(program))
				.leftJoin(submission).on(submission.programProblem.eq(programProblem))
				.leftJoin(programCategory).on(programCategory.program.eq(program))
				.leftJoin(programCategory.category, category)
				.where(isProblemSet, keywordFilter)
				.groupBy(program.id, programType.id)
				.orderBy(program.createdAt.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
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
										problemCountExpr)));

		return new PageImpl<>(content, pageable, total);
	}

	/**
	 * 문제집에 속한 문제로 검색 (페이지네이션) - 매칭된 문제 리스트 포함
	 */
	@Override
	public Page<ProblemSetWithMatchResponseDto> searchProblemSetByProblems(
			String keyword, Pageable pageable) {

		BooleanExpression isProblemSet = program.programType.name.eq("problemset");

		BooleanExpression problemKeywordFilter = problem.title.containsIgnoreCase(keyword);

		// 1단계: 매칭되는 문제집 ID 찾기
		List<Long> programIds = queryFactory
				.selectDistinct(program.id)
				.from(program)
				.join(program.programType, programType)
				.innerJoin(programProblem).on(programProblem.program.eq(program))
				.innerJoin(programProblem.problem, problem)
				.where(isProblemSet, problemKeywordFilter)
				.fetch();

		if (programIds.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, 0);
		}

		// 2단계: 총 개수
		long total = programIds.size();

		// 3단계: 페이지네이션된 ID들
		List<Long> paginatedIds = programIds.stream()
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize())
				.collect(Collectors.toList());

		QSubmission submission = QSubmission.submission;

		NumberExpression<Long> popularityScore = submission.user.id.countDistinct().coalesce(0L);

		NumberExpression<Long> problemCountExpr = programProblem.id.countDistinct().coalesce(0L);

		// 4단계: 문제집 기본 정보 조회 (통계 포함)
		List<ProblemSetWithMatchResponseDto> tempContent = queryFactory
				.from(program)
				.join(program.programType, programType)
				.leftJoin(programProblem).on(programProblem.program.eq(program))
				.leftJoin(submission).on(submission.programProblem.eq(programProblem))
				.leftJoin(programCategory).on(programCategory.program.eq(program))
				.leftJoin(programCategory.category, category)
				.where(program.id.in(paginatedIds))
				.groupBy(program.id, programType.id)
				.orderBy(program.createdAt.desc())
				.transform(
						groupBy(program.id).list(
								Projections.constructor(
										ProblemSetWithMatchResponseDto.class,
										program.id,
										program.title,
										program.description,
										program.thumbnail,
										program.createdAt,
										program.modifiedAt,
										programType.name,
										list(category.name),
										popularityScore,
										problemCountExpr,
										Expressions.constant(List.of()) // 임시 빈 리스트
								)));

		// 5단계: 매칭된 문제 제목 별도 조회
		QProblem matchedProblem = new QProblem("matchedProblem");
		QProgramProblem matchedProgramProblem = new QProgramProblem("matchedProgramProblem");

		Map<Long, List<String>> matchedProblemsMap = queryFactory
				.select(program.id, matchedProblem.title)
				.from(program)
				.innerJoin(matchedProgramProblem).on(matchedProgramProblem.program.eq(program))
				.innerJoin(matchedProgramProblem.problem, matchedProblem)
				.where(
						program.id.in(paginatedIds),
						matchedProblem.title.containsIgnoreCase(keyword))
				.fetch()
				.stream()
				.collect(Collectors.groupingBy(
						tuple -> tuple.get(program.id),
						Collectors.mapping(
								tuple -> tuple.get(matchedProblem.title),
								Collectors.toList())));

		// 6단계: 병합
		List<ProblemSetWithMatchResponseDto> content = tempContent.stream()
				.map(dto -> new ProblemSetWithMatchResponseDto(
						dto.programId(),
						dto.title(),
						dto.description(),
						dto.thumbnail(),
						dto.createAt(),
						dto.modifiedAt(),
						dto.programType(),
						dto.categories(),
						dto.totalParticipants(),
						dto.problemCount(),
						matchedProblemsMap.getOrDefault(dto.programId(), Collections.emptyList()))) // 매칭된 문제 주입
				.collect(Collectors.toList());

		return new PageImpl<>(content, pageable, total);
	}

	@Override
	public Page<ProblemSetWithProgressResponseDto> findMyJoinProblemSets(
			Long userId,
			List<Long> programIds,
			Pageable pageable) {
		QProgram program = QProgram.program;
		QProgramType programType = QProgramType.programType;
		QProgramUser programUser = new QProgramUser("programUser");
		QProgramProblem programProblem = QProgramProblem.programProblem;
		QProgramCategory programCategory = QProgramCategory.programCategory;
		QSubmission submission = QSubmission.submission;

		List<Long> allProgramIds = queryFactory
				.select(program.id)
				.distinct()
				.from(program)
				.innerJoin(program.programType, programType)
				.where(
						program.id.in(programIds),
						programType.id.eq(2L))
				.orderBy(program.createdAt.desc())
				.fetch();

		Map<Long, List<String>> categoriesMap = queryFactory
				.select(program.id, programCategory.category.name)
				.from(programCategory)
				.innerJoin(programCategory.program, program)
				.where(program.id.in(allProgramIds))
				.fetch()
				.stream()
				.collect(Collectors.groupingBy(
						tuple -> tuple.get(0, Long.class),
						Collectors.mapping(
								tuple -> tuple.get(1, String.class),
								Collectors.toList())));

		Map<Long, Long> totalParticipantsMap = queryFactory
				.select(program.id, submission.user.id.countDistinct())
				.from(submission)
				.innerJoin(submission.programProblem, programProblem)
				.innerJoin(programProblem.program, program)
				.where(program.id.in(allProgramIds))
				.groupBy(program.id)
				.fetch()
				.stream()
				.collect(Collectors.toMap(
						tuple -> tuple.get(0, Long.class),
						tuple -> tuple.get(1, Long.class)));

		Map<Long, Long> problemCountMap = queryFactory
				.select(program.id, programProblem.id.countDistinct())
				.from(programProblem)
				.innerJoin(programProblem.program, program)
				.where(program.id.in(allProgramIds))
				.groupBy(program.id)
				.fetch()
				.stream()
				.collect(Collectors.toMap(
						tuple -> tuple.get(0, Long.class),
						tuple -> tuple.get(1, Long.class)));

		// 푼 문제 수 (solvedCount) 계산
		Map<Long, Long> solvedCountMap = queryFactory
				.select(program.id, submission.programProblem.id.countDistinct())
				.from(submission)
				.innerJoin(submission.programProblem, programProblem)
				.innerJoin(programProblem.program, program)
				.where(
						program.id.in(allProgramIds),
						submission.user.id.eq(userId),
						submission.isSuccess.isTrue())
				.groupBy(program.id)
				.fetch()
				.stream()
				.collect(Collectors.toMap(
						tuple -> tuple.get(0, Long.class),
						tuple -> tuple.get(1, Long.class)));

		List<ProblemSetWithProgressResponseDto> content = queryFactory
				.select(
						Projections.constructor(
								ProblemSetWithProgressResponseDto.class,
								program.id,
								program.title,
								program.description,
								program.thumbnail,
								program.createdAt,
								program.modifiedAt,
								programType.name,
								Expressions.asSimple(List.of()),
								Expressions.asSimple(0L),
								Expressions.asSimple(0L),
								Expressions.asSimple(0L))) // solvedCount placeholder
				.from(program)
				.innerJoin(program.programType, programType)
				.where(
						program.id.in(programIds),
						programType.id.eq(2L))
				.distinct()
				.orderBy(program.createdAt.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		content = content.stream()
				.map(dto -> new ProblemSetWithProgressResponseDto(
						dto.programId(),
						dto.title(),
						dto.description(),
						dto.thumbnail(),
						dto.createAt(),
						dto.modifiedAt(),
						dto.programType(),
						categoriesMap.getOrDefault(dto.programId(), List.of()),
						totalParticipantsMap.getOrDefault(dto.programId(), 0L),
						problemCountMap.getOrDefault(dto.programId(), 0L),
						solvedCountMap.getOrDefault(dto.programId(), 0L)))
				.collect(Collectors.toList());

		Long total = (long) allProgramIds.size();

		return new PageImpl<>(content, pageable, total);
	}

	private boolean hasText(String value) {
		return StringUtils.hasText(value);
	}
}
