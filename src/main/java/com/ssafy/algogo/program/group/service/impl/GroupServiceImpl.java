package com.ssafy.algogo.program.group.service.impl;

import com.ssafy.algogo.alarm.entity.AlarmPayload;
import com.ssafy.algogo.alarm.service.AlarmService;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.problem.dto.request.ProgramProblemCreateRequestDto;
import com.ssafy.algogo.problem.dto.request.ProgramProblemDeleteRequestDto;
import com.ssafy.algogo.problem.dto.response.ProgramProblemPageResponseDto;
import com.ssafy.algogo.problem.service.ProgramProblemService;
import com.ssafy.algogo.program.dto.request.ApplyProgramInviteRequestDto;
import com.ssafy.algogo.program.dto.response.GetProgramInviteStateListResponseDto;
import com.ssafy.algogo.program.dto.response.GetProgramJoinStateListResponseDto;
import com.ssafy.algogo.program.entity.InviteStatus;
import com.ssafy.algogo.program.entity.JoinStatus;
import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.entity.ProgramInvite;
import com.ssafy.algogo.program.entity.ProgramJoin;
import com.ssafy.algogo.program.entity.ProgramType;
import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.program.group.dto.request.CheckGroupNameRequestDto;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupInviteStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupJoinStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupMemberRoleRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.CheckGroupNameResponseDto;
import com.ssafy.algogo.program.group.dto.response.GetGroupMemberListResponseDto;
import com.ssafy.algogo.program.group.dto.response.GetGroupMemberResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomPageResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import com.ssafy.algogo.program.group.dto.response.MyGroupRoomPageResponseDto;
import com.ssafy.algogo.program.group.dto.response.MyGroupRoomResponseDto;
import com.ssafy.algogo.program.group.entity.GroupRole;
import com.ssafy.algogo.program.group.entity.GroupRoom;
import com.ssafy.algogo.program.group.entity.GroupsUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.program.group.repository.GroupRepository;
import com.ssafy.algogo.program.group.repository.GroupUserRepository;
import com.ssafy.algogo.program.group.service.GroupService;
import com.ssafy.algogo.program.repository.ProgramInviteRepository;
import com.ssafy.algogo.program.repository.ProgramJoinRepository;
import com.ssafy.algogo.program.repository.ProgramRepository;
import com.ssafy.algogo.program.repository.ProgramTypeRepository;
import com.ssafy.algogo.program.repository.ProgramUserRepository;
import com.ssafy.algogo.program.service.ProgramService;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final ProgramService programService;
    private final ProgramProblemService programProblemService;
    private final AlarmService alarmService;

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ProgramTypeRepository programTypeRepository;
    private final GroupUserRepository groupUserRepository;
    private final ProgramRepository programRepository;
    private final ProgramJoinRepository programJoinRepository;
    private final ProgramInviteRepository programInviteRepository;
    private final ProgramUserRepository programUserRepository;

    @Override
    @Transactional(readOnly = true)
    public GroupRoomPageResponseDto getGroupRoomList(String keyword, Pageable pageable,
        Long userId) {
        if (userId != null) {
            Page<GroupRoomResponseDto> page =
                groupRepository.findAllGroupRoomsWithMemberFlag(keyword, pageable, userId);

            return GroupRoomPageResponseDto.from(page, true);
        } else {
            Page<GroupRoomResponseDto> page =
                groupRepository.findAllGroupRooms(keyword, pageable);

            return GroupRoomPageResponseDto.from(page, false);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GroupRoomResponseDto getGroupRoomDetail(Long programId) {
        GroupRoomResponseDto groupRoomResponseDto = groupRepository.getGroupRoomDetail(programId);

        if (groupRoomResponseDto == null) {
            throw new CustomException("해당 그룹방이 존재하지 않습니다.", ErrorCode.GROUP_NOT_FOUND);
        }

        return groupRoomResponseDto;
    }

    @Override
    public GroupRoomResponseDto createGroupRoom(Long userId,
        CreateGroupRoomRequestDto createGroupRoomRequestDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("userId에 해당하는 데이터가 DB에 없습니다.",
                ErrorCode.USER_NOT_FOUND));

        ProgramType programType = programTypeRepository.findByName("group")
            .orElseThrow(() -> new CustomException("group에 해당하는 데이터가 DB에 없습니다.",
                ErrorCode.PROGRAM_TYPE_NOT_FOUND));

        boolean isTitleConflict = programRepository.existsByTitle(
            createGroupRoomRequestDto.getTitle());
        if (isTitleConflict) {
            throw new CustomException("이미 존재하는 그룹명이 있습니다.", ErrorCode.DUPLICATE_RESOURCE);
        }

        GroupRoom groupRoom = GroupRoom.create(
            createGroupRoomRequestDto.getTitle(),
            createGroupRoomRequestDto.getDescription(),
            programType,
            createGroupRoomRequestDto.getCapacity()
        );
        groupRepository.save(groupRoom);

        GroupsUser groupsUser = GroupsUser.create(
            ProgramUserStatus.ACTIVE,
            groupRoom,
            user,
            GroupRole.ADMIN
        );
        groupUserRepository.save(groupsUser);

        return groupRepository.getGroupRoomDetail(groupRoom.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CheckGroupNameResponseDto checkGroupName(
        CheckGroupNameRequestDto checkGroupNameRequestDto) {
        return new CheckGroupNameResponseDto(
            !programRepository.existsByTitle(checkGroupNameRequestDto.getGroupTitle()));
    }

    @Override
    public GroupRoomResponseDto updateGroupRoom(Long programId,
        UpdateGroupRoomRequestDto updateGroupRoomRequestDto) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        if (updateGroupRoomRequestDto.getTitle() != null &&
            !updateGroupRoomRequestDto.getTitle().equals(groupRoom.getTitle())) {

            boolean isTitleConflict = programRepository.existsByTitle(
                updateGroupRoomRequestDto.getTitle());
            if (isTitleConflict) {
                throw new CustomException("이미 존재하는 그룹명이 있습니다.", ErrorCode.DUPLICATE_RESOURCE);
            }
        }

        groupRoom.updateGroupRoom(
            updateGroupRoomRequestDto.getTitle(),
            updateGroupRoomRequestDto.getDescription(),
            updateGroupRoomRequestDto.getCapacity()
        );

        groupRepository.save(groupRoom);

        return groupRepository.getGroupRoomDetail(groupRoom.getId());
    }

    @Override
    public void deleteGroupRoom(Long programId) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(() -> new CustomException(
                "해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        groupRepository.delete(groupRoom);
    }

    @Override
    public void applyGroupJoin(Long userId, Long programId) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        programService.applyProgramJoin(userId, programId);

        User admin = groupUserRepository.findAdminByProgramId(programId)
            .orElseThrow(
                () -> new CustomException("방장을 찾을 수 없습니다.", ErrorCode.GROUP_USER_NOT_FOUND));

        // 그룹의 방장에게 알람 전송
        alarmService.createAndSendAlarm(
            admin.getId(),
            "GROUP_JOIN_APPLY",
            new AlarmPayload(null, null, null, programId, userId),
            "새로운 참여 신청이 도착했습니다."
        );
    }

    @Override
    public void updateGroupJoinState(Long userId, Long programId, Long joinId,
        UpdateGroupJoinStateRequestDto updateGroupJoinStateRequestDto) {

        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        // 해당 신청(joinId)이 실제 존재하는지 확인
        ProgramJoin programJoin = programJoinRepository.findById(joinId)
            .orElseThrow(() ->
                new CustomException("해당 참여 신청 정보를 찾을 수 없습니다.", ErrorCode.PROGRAM_JOIN_NOT_FOUND));

        // 신청이 해당 programId에 속해야 함
        if (!programJoin.getProgram().getId().equals(programId)) {
            throw new CustomException("해당 프로그램의 신청이 아닙니다.", ErrorCode.INVALID_PARAMETER);
        }

        // 이미 처리된 신청이면 거부
        if (programJoin.getJoinStatus() != JoinStatus.PENDING) {
            throw new CustomException("이미 처리된 신청입니다.", ErrorCode.BAD_REQUEST);
        }

        Long applicantId = programJoin.getUser().getId();  // 신청자 ID

        // 처리 로직
        Program program = programJoin.getProgram();
        User applicant = programJoin.getUser();
        if (updateGroupJoinStateRequestDto.getIsAccepted().equals("ACCEPTED")) {

            // 신청자가 이미 ACTIVE 상태인지 검사
            Optional<ProgramUser> existingUserInProgram =
                programUserRepository.findByUserIdAndProgramIdAndProgramUserStatus(
                    applicantId, programId, ProgramUserStatus.ACTIVE);
            if (existingUserInProgram.isPresent()) {
                throw new CustomException("이미 프로그램에 참여한 회원입니다.", ErrorCode.PROGRAM_ALREADY_JOINED);
            }

            // 신청 승인 → 프로그램 유저 등록
            GroupsUser groupsUser = GroupsUser.create(ProgramUserStatus.ACTIVE, program, applicant,
                GroupRole.USER);
            groupUserRepository.save(groupsUser);

            // program_join 상태 업데이트
            programJoin.updateJoinStatus(JoinStatus.ACCEPTED);
            programJoinRepository.save(programJoin);

        } else if (updateGroupJoinStateRequestDto.getIsAccepted().equals("DENIED")) {

            programJoin.updateJoinStatus(JoinStatus.DENIED);
            programJoinRepository.save(programJoin);

        }

        // 신청한 사람한테 알람 전송
        alarmService.createAndSendAlarm(
            applicant.getId(),
            "GROUP_JOIN_UPDATE",
            new AlarmPayload(null, null, null, programId, null),
            "참여 신청이 '" + updateGroupJoinStateRequestDto.getIsAccepted() + "' 처리되었습니다."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GetProgramJoinStateListResponseDto getGroupJoinState(Long programId) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        return programService.getProgramJoinState(programId);
    }

    @Override
    public void applyGroupInvite(Long programId,
        ApplyProgramInviteRequestDto applyProgramInviteRequestDto) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        programService.applyProgramInvite(programId, applyProgramInviteRequestDto);

        // 초대 받은 사람한테 알람 전송
        alarmService.createAndSendAlarm(
            applyProgramInviteRequestDto.getUserId(),
            "GROUP_INVITE_APPLY",
            new AlarmPayload(null, null, null, programId, null),
            "그룹 초대가 도착했습니다."
        );
    }

    @Override
    public void updateGroupInviteState(Long userId, Long programId, Long inviteId,
        UpdateGroupInviteStateRequestDto updateGroupInviteStateRequestDto) {

        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        // 해당 초대가 실제 존재하는지 확인
        ProgramInvite programInvite = programInviteRepository.findById(inviteId)
            .orElseThrow(() -> new CustomException("해당 초대 정보를 찾을 수 없습니다.",
                ErrorCode.PROGRAM_INVITE_NOT_FOUND));

        // 초대가 해당 programId에 속해야 함
        if (!programInvite.getProgram().getId().equals(programId)) {
            throw new CustomException("해당 프로그램의 초대가 아닙니다.", ErrorCode.INVALID_PARAMETER);
        }

        // 초대가 해당 userId에 속해야 함
        if (!programInvite.getUser().getId().equals(userId)) {
            throw new CustomException("해당 유저의 초대가 아닙니다.", ErrorCode.INVALID_PARAMETER);
        }

        // 이미 처리된 초대면 거부
        if (programInvite.getInviteStatus() != InviteStatus.PENDING) {
            throw new CustomException("이미 처리된 초대입니다.", ErrorCode.BAD_REQUEST);
        }

        // 처리 로직
        if (updateGroupInviteStateRequestDto.getIsAccepted().equals("ACCEPTED")) {

            // 신청자가 이미 ACTIVE 상태인지 검사
            Optional<ProgramUser> existingUserInProgram =
                programUserRepository.findByUserIdAndProgramIdAndProgramUserStatus(
                    userId, programId, ProgramUserStatus.ACTIVE);
            if (existingUserInProgram.isPresent()) {
                throw new CustomException("이미 프로그램에 참여한 회원입니다.", ErrorCode.PROGRAM_ALREADY_JOINED);
            }

            // 초대 승인 → 프로그램에 사용자를 추가
            Program program = programInvite.getProgram();
            User user = programInvite.getUser();

            // ProgramUser 생성 (사용자가 그룹에 참여하도록 설정)
            GroupsUser groupsUser = GroupsUser.create(ProgramUserStatus.ACTIVE, program, user,
                GroupRole.USER);
            programUserRepository.save(groupsUser);

            // 초대 상태를 ACCEPTED로 변경
            programInvite.updateInviteStatus(InviteStatus.ACCEPTED);
            programInviteRepository.save(programInvite);

        } else if (updateGroupInviteStateRequestDto.getIsAccepted().equals("DENIED")) {

            // 초대 거절 → 상태만 변경
            programInvite.updateInviteStatus(InviteStatus.DENIED);
            programInviteRepository.save(programInvite);
        }

        User admin = groupUserRepository.findAdminByProgramId(programId)
            .orElseThrow(
                () -> new CustomException("방장을 찾을 수 없습니다.", ErrorCode.GROUP_USER_NOT_FOUND));

        // 방장한테 알람 전송
        alarmService.createAndSendAlarm(
            admin.getId(),
            "GROUP_INVITE_UPDATE",
            new AlarmPayload(null, null, null, programId, userId),
            "초대받은 사용자가 초대를 '" + updateGroupInviteStateRequestDto.getIsAccepted() + "' 처리했습니다."
        );
    }

    @Override
    public void deleteGroupInvite(Long programId, Long inviteId) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        programService.deleteProgramInvite(programId, inviteId);
    }

    @Override
    @Transactional(readOnly = true)
    public GetProgramInviteStateListResponseDto getGroupInviteState(Long programId) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        return programService.getProgramInviteState(programId);
    }

    @Override
    @Transactional(readOnly = true)
    public GetGroupMemberListResponseDto getGroupMember(Long programId) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        List<GroupsUser> groupUsers = groupUserRepository.findByProgramIdAndProgramUserStatusWithUser(
            programId, ProgramUserStatus.ACTIVE);

        List<GetGroupMemberResponseDto> members = groupUsers.stream()
            .map(GetGroupMemberResponseDto::from)
            .collect(Collectors.toList());

        return new GetGroupMemberListResponseDto(members);
    }

    @Override
    public void updateGroupMemberRole(Long programId, Long programUserId,
        UpdateGroupMemberRoleRequestDto updateGroupMemberRoleRequestDto) {

        // 프로그램이 그룹인지 확인
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        // 그룹에 해당 프로그램 유저가 존재하는지 확인
        GroupsUser groupUser = groupUserRepository.findById(programUserId)
            .orElseThrow(
                () -> new CustomException("해당 그룹의 멤버가 아닙니다.", ErrorCode.GROUP_USER_NOT_FOUND));

        // 해당 유저가 active가 아닌 경우
        if (groupUser.getProgramUserStatus() != ProgramUserStatus.ACTIVE) {
            throw new CustomException("해당 멤버는 ACTIVE 상태가 아닙니다.", ErrorCode.GROUP_USER_NOT_FOUND);
        }

        if (!groupRoom.getId().equals(groupUser.getProgram().getId())) {
            throw new CustomException("프로그램 id 정보와 프로그램 회원 id 정보가 매치하지 않습니다",
                ErrorCode.BAD_REQUEST);
        }

        // 변경하려는 role이 기존 역할과 동일한지 확인
        if (groupUser.getGroupRole().name().equals(updateGroupMemberRoleRequestDto.getRole())) {
            throw new CustomException("현재 역할과 동일한 권한으로 변경할 수 없습니다.", ErrorCode.BAD_REQUEST);
        }

        // admin의 권환을 바꾸려고 하는 경우
        if (groupUser.getGroupRole() == GroupRole.ADMIN) {
            throw new CustomException("ADMIN의 권한은 바꿀 수 없습니다.", ErrorCode.BAD_REQUEST);
        }

        GroupRole newRole = GroupRole.valueOf(updateGroupMemberRoleRequestDto.getRole());
        groupUser.updateRole(newRole);

        groupUserRepository.save(groupUser);

    }

    @Override
    public void deleteGroupMember(Long userId, Long programId, Long programUserId) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        GroupsUser tryUser = groupUserRepository.findByProgramIdAndUserIdWithUser(programId, userId)
            .orElseThrow(() -> new CustomException("삭제를 시도하는 사용자를 찾을 수 없습니다.",
                ErrorCode.GROUP_USER_NOT_FOUND));

        GroupsUser targetUser = groupUserRepository.findById(programUserId)
            .orElseThrow(() -> new CustomException("삭제 대상이 되는 사용자를 찾을 수 없습니다.",
                ErrorCode.GROUP_USER_NOT_FOUND));

        // 대상이 이미 삭제된 경우
        if (targetUser.getProgramUserStatus() == ProgramUserStatus.WITHDRAW) {
            throw new CustomException("이미 삭제된 사용자 입니다.", ErrorCode.DUPLICATE_RESOURCE);
        }

        if (tryUser.getGroupRole() == GroupRole.ADMIN) {
            // 관리자는 자신을 제외한 다른 유저 삭제 가능
            if (tryUser.getId().equals(targetUser.getId())) {
                throw new CustomException("관리자는 자신을 삭제할 수 없습니다.", ErrorCode.BAD_REQUEST);
            }

            targetUser.updateProgramUserStatus(ProgramUserStatus.WITHDRAW);
            groupUserRepository.save(targetUser);
        }
        // 일반 사용자(USER) 또는 관리자(MANAGER)일 경우
        else if (tryUser.getGroupRole() == GroupRole.USER
            || tryUser.getGroupRole() == GroupRole.MANAGER) {
            // 자신만 삭제 가능
            if (!tryUser.getId().equals(targetUser.getId())) {
                throw new CustomException("관라지가 아닌 멤버는 자기 자신만 삭제할 수 있습니다.", ErrorCode.BAD_REQUEST);
            }

            targetUser.updateProgramUserStatus(ProgramUserStatus.WITHDRAW);
            groupUserRepository.save(targetUser);
        }
    }

    @Override
    public void addGroupProblem(Long programId,
        ProgramProblemCreateRequestDto programProblemCreateRequestDto) {

        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        programProblemService.createProgramProblem(programId, programProblemCreateRequestDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramProblemPageResponseDto getAllGroupProblems(Long programId, Pageable pageable) {

        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        return programProblemService.getAllProgramProblems(programId, pageable);
    }

    @Override
    public void deleteGroupProblems(Long programId,
        ProgramProblemDeleteRequestDto programProblemDeleteRequestDto) {
        GroupRoom groupRoom = groupRepository.findById(programId)
            .orElseThrow(
                () -> new CustomException("해당 그룹방을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND));

        programProblemService.deleteProgramProblem(programId, programProblemDeleteRequestDto);
    }

    @Override
    @Transactional(readOnly = true)
    public MyGroupRoomPageResponseDto getMyGroupRooms(Long userId, Pageable pageable) {

        List<Long> programIds =
            groupUserRepository.findActiveProgramIdsByUserId(userId);

        // 없으면 빈 pageable 객체 반환
        if (programIds.isEmpty()) {
            return MyGroupRoomPageResponseDto.from(Page.empty(pageable));
        }

        Page<MyGroupRoomResponseDto> page =
            groupRepository.findMyGroupRooms(programIds, userId, pageable);

        return MyGroupRoomPageResponseDto.from(page);
    }


}
