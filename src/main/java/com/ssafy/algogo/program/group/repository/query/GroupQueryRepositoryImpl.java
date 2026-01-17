package com.ssafy.algogo.program.group.repository.query;

import static com.ssafy.algogo.problem.entity.QProgramProblem.programProblem;
import static com.ssafy.algogo.program.entity.QProgram.program;
import static com.ssafy.algogo.program.group.entity.QGroupRoom.groupRoom;
import static com.ssafy.algogo.program.entity.QProgramInvite.programInvite;
import static com.ssafy.algogo.program.entity.QProgramJoin.programJoin;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.program.group.dto.response.GetReceivedGroupInviteResponseDto;
import com.ssafy.algogo.program.group.dto.response.GetSentGroupJoinResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import com.ssafy.algogo.program.group.dto.response.MyGroupRoomResponseDto;
import com.ssafy.algogo.program.group.entity.GroupRole;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.program.group.entity.QGroupsUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupQueryRepositoryImpl implements GroupQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public Page<GroupRoomResponseDto> findAllGroupRooms(String keyword, Pageable pageable) {

        BooleanExpression condition = buildSearchCondition(keyword);

        QGroupsUser memberUser = new QGroupsUser("memberUser");

        List<GroupRoomResponseDto> content = query
            .select(Projections.constructor(
                GroupRoomResponseDto.class,
                groupRoom.id,
                groupRoom.title,
                groupRoom.description,
                groupRoom.createdAt,
                groupRoom.modifiedAt,
                groupRoom.capacity,
                memberUser.id.countDistinct(),
                programProblem.id.countDistinct(),
                Expressions.nullExpression(Boolean.class),
                Expressions.nullExpression(GroupRole.class)
            ))
            .from(groupRoom)
            .leftJoin(memberUser).on(memberUser.program.id.eq(groupRoom.id)
                .and(memberUser.programUserStatus.eq(ProgramUserStatus.ACTIVE)))
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
    public GroupRoomResponseDto getGroupRoomDetailWithUser(Long programId, Long userId) {
        QGroupsUser memberUser = new QGroupsUser("memberUser");
        QGroupsUser selfUser = new QGroupsUser("selfUser");

        return query
            .select(Projections.constructor(
                GroupRoomResponseDto.class,
                groupRoom.id,
                program.title,
                program.description,
                program.createdAt,
                program.modifiedAt,
                groupRoom.capacity,
                memberUser.id.countDistinct(),
                programProblem.id.countDistinct(),
                selfUser.id.isNotNull(),
                selfUser.groupRole
            ))
            .from(groupRoom)
            .join(program).on(program.id.eq(groupRoom.id))
            .leftJoin(memberUser).on(memberUser.program.id.eq(groupRoom.id)
                .and(memberUser.programUserStatus.eq(ProgramUserStatus.ACTIVE)))
            .leftJoin(programProblem).on(programProblem.program.id.eq(groupRoom.id))
            .leftJoin(selfUser).on(selfUser.program.id.eq(groupRoom.id)
                .and(selfUser.user.id.eq(userId))
                .and(selfUser.programUserStatus.eq(ProgramUserStatus.ACTIVE)))
            .where(groupRoom.id.eq(programId))
            .groupBy(
                groupRoom.id,
                program.id,
                selfUser.id,
                selfUser.groupRole
            )
            .fetchOne();
    }

    @Override
    public GroupRoomResponseDto getGroupRoomDetail(Long programId) {

        QGroupsUser memberUser = new QGroupsUser("memberUser");

        return query
            .select(Projections.constructor(
                GroupRoomResponseDto.class,
                groupRoom.id,
                program.title,
                program.description,
                program.createdAt,
                program.modifiedAt,
                groupRoom.capacity,
                memberUser.id.countDistinct(),
                programProblem.id.countDistinct(),
                Expressions.nullExpression(Boolean.class),
                Expressions.nullExpression(GroupRole.class)
            ))
            .from(groupRoom)
            .join(program).on(program.id.eq(groupRoom.id))
            .leftJoin(memberUser).on(memberUser.program.id.eq(groupRoom.id)
                .and(memberUser.programUserStatus.eq(ProgramUserStatus.ACTIVE)))
            .leftJoin(programProblem).on(programProblem.program.id.eq(groupRoom.id))
            .where(groupRoom.id.eq(programId))
            .groupBy(groupRoom.id, program.id)
            .fetchOne();
    }

    @Override
    public Page<MyGroupRoomResponseDto> findMyGroupRooms(
        List<Long> programIds,
        Long userId,
        Pageable pageable
    ) {

        QGroupsUser memberUser = new QGroupsUser("memberUser");
        QGroupsUser selfUser = new QGroupsUser("selfUser");

        List<MyGroupRoomResponseDto> content = query
            .select(Projections.constructor(
                MyGroupRoomResponseDto.class,
                groupRoom.id,
                groupRoom.title,
                groupRoom.description,
                groupRoom.createdAt,
                groupRoom.modifiedAt,
                groupRoom.capacity,
                memberUser.id.countDistinct(),
                programProblem.id.countDistinct(),
                selfUser.groupRole
            ))
            .from(groupRoom)
            .join(selfUser).on(selfUser.program.id.eq(groupRoom.id)
                .and(selfUser.user.id.eq(userId))
                .and(selfUser.programUserStatus.eq(ProgramUserStatus.ACTIVE)))
            .leftJoin(memberUser).on(memberUser.program.id.eq(groupRoom.id)
                .and(memberUser.programUserStatus.eq(ProgramUserStatus.ACTIVE)))
            .leftJoin(programProblem).on(programProblem.program.id.eq(groupRoom.id))
            .where(groupRoom.id.in(programIds))
            .groupBy(
                groupRoom.id,
                selfUser.groupRole
            )
            .orderBy(getOrderSpecifiers(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = query
            .select(groupRoom.countDistinct())
            .from(groupRoom)
            .where(groupRoom.id.in(programIds))
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
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

    @Override
    public Page<GroupRoomResponseDto> findAllGroupRoomsWithMemberFlag(String keyword,
        Pageable pageable, Long userId) {

        BooleanExpression condition = buildSearchCondition(keyword);

        QGroupsUser memberUser = new QGroupsUser("memberUser"); // 전체 멤버 카운트용
        QGroupsUser selfUser = new QGroupsUser("selfUser"); // 로그인 유저 전용

        List<GroupRoomResponseDto> content = query
            .select(Projections.constructor(
                GroupRoomResponseDto.class,
                groupRoom.id,
                groupRoom.title,
                groupRoom.description,
                groupRoom.createdAt,
                groupRoom.modifiedAt,
                groupRoom.capacity,
                memberUser.id.countDistinct(),
                programProblem.id.countDistinct(),
                selfUser.id.isNotNull(),  // 멤버 여부
                selfUser.groupRole
            ))
            .from(groupRoom)
            .leftJoin(memberUser).on(memberUser.program.id.eq(groupRoom.id)
                .and(memberUser.programUserStatus.eq(ProgramUserStatus.ACTIVE)))
            .leftJoin(programProblem).on(programProblem.program.id.eq(groupRoom.id))
            .leftJoin(selfUser).on(selfUser.program.id.eq(groupRoom.id)
                .and(selfUser.user.id.eq(userId))
                .and(selfUser.programUserStatus.eq(ProgramUserStatus.ACTIVE)))
            .where(condition)
            .groupBy(
                groupRoom.id,
                selfUser.id,
                selfUser.groupRole
            )
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
    public List<GetReceivedGroupInviteResponseDto> findReceivedGroupInvitesWithRoom(Long userId) {
        QGroupsUser memberUser = new QGroupsUser("memberUser"); // 전체 멤버 수
        QGroupsUser selfUser = new QGroupsUser("selfUser");     // 로그인 유저 기준

        return query
            .select(Projections.constructor(
                GetReceivedGroupInviteResponseDto.class,
                programInvite.id,
                programInvite.inviteStatus,
                // GroupRoomResponseDto
                Projections.constructor(
                    GroupRoomResponseDto.class,
                    groupRoom.id,
                    program.title,
                    program.description,
                    program.createdAt,
                    program.modifiedAt,
                    groupRoom.capacity,
                    memberUser.id.countDistinct(),
                    programProblem.id.countDistinct(),
                    selfUser.id.isNotNull(),
                    selfUser.groupRole
                )
            ))
            .from(programInvite)
            .join(groupRoom).on(programInvite.program.id.eq(groupRoom.id))
            .join(program).on(program.id.eq(groupRoom.id))

            // 멤버 수
            .leftJoin(memberUser).on(
                memberUser.program.id.eq(groupRoom.id)
                    .and(memberUser.programUserStatus.eq(ProgramUserStatus.ACTIVE))
            )

            // 문제 수
            .leftJoin(programProblem)
            .on(programProblem.program.id.eq(groupRoom.id))

            // 로그인 유저 기준 정보
            .leftJoin(selfUser).on(
                selfUser.program.id.eq(groupRoom.id)
                    .and(selfUser.user.id.eq(userId))
                    .and(selfUser.programUserStatus.eq(ProgramUserStatus.ACTIVE))
            )

            // 내가 받은 초대
            .where(programInvite.user.id.eq(userId))

            .groupBy(
                programInvite.id,
                programInvite.inviteStatus,
                groupRoom.id,
                program.id,
                selfUser.id,
                selfUser.groupRole
            )
            .fetch();
    }

    @Override
    public List<GetSentGroupJoinResponseDto> findSentGroupJoinRequestsWithRoom(Long userId) {

        QGroupsUser memberUser = new QGroupsUser("memberUser"); // 전체 멤버 수
        QGroupsUser selfUser = new QGroupsUser("selfUser");     // 로그인 유저 기준

        return query
            .select(Projections.constructor(
                GetSentGroupJoinResponseDto.class,
                programJoin.id,
                programJoin.joinStatus,

                // GroupRoomResponseDto
                Projections.constructor(
                    GroupRoomResponseDto.class,
                    groupRoom.id,
                    program.title,
                    program.description,
                    program.createdAt,
                    program.modifiedAt,
                    groupRoom.capacity,
                    memberUser.id.countDistinct(),
                    programProblem.id.countDistinct(),
                    selfUser.id.isNotNull(),
                    selfUser.groupRole
                )
            ))
            .from(programJoin)
            .join(groupRoom).on(programJoin.program.id.eq(groupRoom.id))
            .join(program).on(program.id.eq(groupRoom.id))

            // 멤버 수
            .leftJoin(memberUser).on(
                memberUser.program.id.eq(groupRoom.id)
                    .and(memberUser.programUserStatus.eq(ProgramUserStatus.ACTIVE))
            )

            // 문제 수
            .leftJoin(programProblem)
            .on(programProblem.program.id.eq(groupRoom.id))

            // 로그인 유저 기준 정보
            .leftJoin(selfUser).on(
                selfUser.program.id.eq(groupRoom.id)
                    .and(selfUser.user.id.eq(userId))
                    .and(selfUser.programUserStatus.eq(ProgramUserStatus.ACTIVE))
            )

            // 내가 보낸 신청
            .where(programJoin.user.id.eq(userId))

            .groupBy(
                programJoin.id,
                programJoin.joinStatus,
                groupRoom.id,
                program.id,
                selfUser.id,
                selfUser.groupRole
            )
            .fetch();
    }
}
