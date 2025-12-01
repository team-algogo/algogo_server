package com.ssafy.algogo.program.group.repository.query;

import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.program.entity.QProgramUser.programUser;
import static com.ssafy.algogo.program.group.entity.QGroupRoom.groupRoom;
import static com.ssafy.algogo.program.group.entity.QGroupsUser.groupsUser;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupQueryRepositoryImpl implements GroupQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public Page<GroupRoomResponseDto> findAllGroupRooms(String keyword, Pageable pageable) {

        BooleanExpression condition = buildSearchCondition(keyword);

        List<GroupRoomResponseDto> content = query
            .select(Projections.constructor(
                GroupRoomResponseDto.class,
                groupRoom.id,
                groupRoom.title,
                groupRoom.description,
                groupRoom.createdAt,
                groupRoom.modifiedAt,
                groupRoom.capacity,
                groupsUser.id.countDistinct(),
                programProblem.id.countDistinct()
            ))
            .from(groupRoom)
            .leftJoin(groupsUser).on(groupsUser.program.id.eq(groupRoom.id))
            .leftJoin(programProblem).on(programProblem.program.id.eq(groupRoom.id))
            .where(condition)
            .groupBy(groupRoom.id)
            .orderBy(getOrderSpecifiers(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long totalCount = query
            .select(groupRoom.countDistinct())
            .from(groupRoom)
            .where(condition)
            .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public GroupRoomResponseDto getGroupRoomDetail(Long programId) {

        return query
            .select(Projections.constructor(
                GroupRoomResponseDto.class,
                program.id,
                program.title,
                program.description,
                program.createdAt,
                program.modifiedAt,
                groupRoom.capacity,
                programUser.id.countDistinct(),
                programProblem.id.countDistinct()
            ))
            .from(program)
            .join(groupRoom).on(groupRoom.id.eq(program.id))
            .leftJoin(programUser).on(programUser.program.id.eq(programId))
            .leftJoin(programProblem).on(programProblem.program.id.eq(programId))
            .where(program.id.eq(programId))
            .groupBy(program.id, groupRoom.capacity)
            .fetchOne();
    }


    // pageable 기반 OrderSpecifier 가져오는 로직 음 좀 더러운 거 같은데, 이거 공부하고 추후 보완
    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        // sort 조건이 없으면 기본 createdAt DESC
        if (pageable.getSort().isEmpty()) {
            return new OrderSpecifier<?>[]{groupRoom.createdAt.desc()};
        }

        return pageable.getSort().stream()
            .map(order -> {
                String property = order.getProperty(); // e.g. "createdAt"
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                return switch (property) {
                    case "id" -> new OrderSpecifier<>(direction, groupRoom.id);
                    case "title" -> new OrderSpecifier<>(direction, groupRoom.title);
                    case "description" -> new OrderSpecifier<>(direction, groupRoom.description);
                    case "modifiedAt" -> new OrderSpecifier<>(direction, groupRoom.modifiedAt);
                    case "createdAt" -> new OrderSpecifier<>(direction, groupRoom.createdAt);
                    // 허용되지 않은 필드는 createdAt DESC로 강제
                    default -> new OrderSpecifier<>(Order.DESC, groupRoom.createdAt);
                };
            })
            .toArray(OrderSpecifier[]::new);
    }

    private BooleanExpression buildSearchCondition(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return groupRoom.title.containsIgnoreCase(keyword)
            .or(groupRoom.description.containsIgnoreCase(keyword));
    }
}
