package com.ssafy.algogo.program.service;

import com.ssafy.algogo.program.dto.request.ApplyProgramInviteRequestDto;
import com.ssafy.algogo.program.dto.response.GetProgramInviteStateListResponseDto;
import com.ssafy.algogo.program.dto.response.GetProgramJoinStateListResponseDto;

public interface ProgramService {

    void applyProgramJoin(Long userId, Long programId);

    GetProgramJoinStateListResponseDto getProgramJoinState(Long programId);

    void applyProgramInvite(Long programId,
        ApplyProgramInviteRequestDto applyProgramInviteRequestDto);

    void deleteProgramInvite(Long programId, Long inviteId);

    GetProgramInviteStateListResponseDto getProgramInviteState(Long programId);

}
