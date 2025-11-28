package com.ssafy.algogo.program.group.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.ssafy.algogo.problem.entity.QProgramProblem.*;
import static com.ssafy.algogo.program.entity.QProgram.*;
import static com.ssafy.algogo.program.entity.QProgramUser.*;
import static com.ssafy.algogo.program.group.entity.QGroupRoom.*;

@Repository
@RequiredArgsConstructor
public class GroupQueryRepositoryImpl implements GroupQueryRepository {

    private final JPAQueryFactory query;

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
}
