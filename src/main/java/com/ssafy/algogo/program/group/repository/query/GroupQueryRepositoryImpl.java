package com.ssafy.algogo.program.group.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.problem.entity.QProgramProblem;
import com.ssafy.algogo.program.entity.QProgram;
import com.ssafy.algogo.program.entity.QProgramUser;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import com.ssafy.algogo.program.group.entity.QGroupRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupQueryRepositoryImpl implements GroupQueryRepository{

  private final JPAQueryFactory query;

  @Override
  public GroupRoomResponseDto getGroupRoomDetail(Long programId) {
    QProgram program = QProgram.program;
    QGroupRoom groupRoom = QGroupRoom.groupRoom;
    QProgramUser programUser = QProgramUser.programUser;
    QProgramProblem programProblem = QProgramProblem.programProblem;

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
