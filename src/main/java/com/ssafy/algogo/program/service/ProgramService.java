package com.ssafy.algogo.program.service;

import com.ssafy.algogo.program.dto.response.GetGroupJoinStateListResponseDto;

public interface ProgramService {
  void applyProgramJoin(Long userId, Long programId);

  GetGroupJoinStateListResponseDto getProgramJoinState(Long programId);
}
