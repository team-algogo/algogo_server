package com.ssafy.algogo.common.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /* ============================================================
     * COMMON (400 / 404 / 409 등)
     * 공통적인 오류 — 어떤 도메인에도 속하지 않는 기본 에러
     * ============================================================ */

    /** 요청 형식이 잘못되었을 때 (잘못된 JSON, 누락된 필드 등) */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST"),

    /** 파라미터가 형식에 맞지 않거나 잘못된 값일 때 */
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER"),

    /** 필수 파라미터가 누락된 경우 (NotNull, NotBlank 위반 등) */
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER"),

    /** 숫자여야 하는데 문자열이 들어온 경우 등 타입 불일치 */
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH"),

    /** 특정 리소스를 찾을 수 없을 때 사용 (세분화된 NotFound가 없을 경우, DB 조회했는데 해당 데이터가 없는 경우) */
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND"),

    /** 중복된 리소스 생성 시 (이메일 중복, 닉네임 중복 등 도메인별 항목 없으면 사용) */
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE"),

    /* ============================================================
     * AUTH (401 / 403)
     * 인증/인가 관련 오류 — 로그인, 토큰, 권한 체크
     * ============================================================ */

    /** 인증이 필요한 요청에서 JWT 또는 로그인 정보가 없을 때 */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"),

    /** 토큰이 잘못되었거나 변조된 경우 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN"),

    /** 토큰이 만료된 경우 (프론트는 재발급 요청하도록 유도) */
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN"),

    /** 지원하지 않는 토큰 타입 */
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "UNSUPPORTED_TOKEN"),

    /** 권한이 부족한 경우 (예: 방장만 가능한 API 요청) */
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN"),

    /** 접근 권한 없음 — 주로 ROLE 부족 */
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED"),

    /* ============================================================
     * USER (회원)
     * 회원 가입/조회/수정/중복 체크 등
     * ============================================================ */

    /** 유저 ID로 조회했을 때 해당 유저가 존재하지 않을 때 */
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"),

    /** 로그인 시 잘못된 이메일/비번 입력 */
    INVALID_LOGIN_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN_CREDENTIALS"),

    /** 프로필 이미지가 없는 경우 (수정·삭제 시 사용) */
    PROFILE_IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROFILE_IMAGE_NOT_FOUND"),

    /* ============================================================
     * Program (프로그램 전체 관려)
     * 프로그램, 프로그램 타입 관련
     * ============================================================ */
    /** 프로그램 타입이 존재하지 않음 */
    PROGRAM_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROGRAM_TYPE_NOT_FOUND"),

    /** 사용자가 이미 그룹에 존재할 경우 발생시킬 conflict 에러*/
    PROGRAM_ALREADY_JOINED(HttpStatus.CONFLICT, "PROGRAM_ALREADY_JOINED"),

    PROGRAM_JOIN_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROGRAM_JOIN_NOT_FOUND"),

    PROGRAM_INVITE_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROGRAM_INVITE_NOT_FOUND"),

    /* ============================================================
     * GROUP (그룹 / 그룹방)
     * 그룹 생성, 수정, 삭제, 그룹 멤버, 초대, 참여 관련
     * ============================================================ */

    /** 그룹 ID로 조회 시 해당 그룹이 존재하지 않음 */
    GROUP_NOT_FOUND(HttpStatus.BAD_REQUEST, "GROUP_NOT_FOUND"),

    /** 그룹에 대한 접근 권한 없음 (방장/매니저 제한 기능) */
    GROUP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "GROUP_ACCESS_DENIED"),

    /** 그룹 내 특정 사용자 정보가 없음 (ex: program_user_id 잘못됨) */
    GROUP_USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "GROUP_USER_NOT_FOUND"),

    /** 초대 엔티티가 존재하지 않음 (invite_id 잘못된 경우) */
    GROUP_INVITE_NOT_FOUND(HttpStatus.BAD_REQUEST, "GROUP_INVITE_NOT_FOUND"),

    /** 참여 신청 엔티티가 존재하지 않음 (join_id 잘못된 경우) */
    GROUP_JOIN_NOT_FOUND(HttpStatus.BAD_REQUEST, "GROUP_JOIN_NOT_FOUND"),

    /* ============================================================
     * PROBLEM SET (문제집)
     * 문제집 조회/생성/수정/삭제 및 문제 추가/삭제
     * ============================================================ */

    /** problem_set 또는 program_id 기반 조회 실패 */
    PROBLEM_SET_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROBLEM_SET_NOT_FOUND"),

    /** 이미 존재하는 문제집 제목으로 생성하려는 경우 */
    PROBLEM_SET_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROBLEM_SET_ALREADY_EXISTS"),

    /** URL 경로의 program_id가 유효하지 않음 */
    PROGRAM_ID_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROGRAM_ID_NOT_FOUND"),

    /** 문제집 내부 문제(program_problem)가 존재하지 않을 때 */
    PROGRAM_PROBLEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROGRAM_PROBLEM_NOT_FOUND"),

    /** 문제집에 동일한 문제가 이미 포함된 경우 */
    PROBLEM_ALREADY_EXISTS_IN_SET(HttpStatus.CONFLICT, "PROBLEM_ALREADY_EXISTS_IN_SET"),

    /* ============================================================
     * PROBLEM / CAMPAIGN (문제, 캠페인)
     * ============================================================ */

    /** problem_id 기반 조회 실패 */
    PROBLEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROBLEM_NOT_FOUND"),

    /** 캠페인 ID 기반 조회 실패 */
    CAMPAIGN_NOT_FOUND(HttpStatus.BAD_REQUEST, "CAMPAIGN_NOT_FOUND"),

    /** 이미 존재하는 캠페인 이름/리소스 */
    CAMPAIGN_ALREADY_EXISTS(HttpStatus.CONFLICT, "CAMPAIGN_ALREADY_EXISTS"),

    /** 캠페인 문제 조회 실패 */
    CAMPAIGN_PROBLEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "CAMPAIGN_PROBLEM_NOT_FOUND"),

    /* ============================================================
     * SUBMISSION (코드 제출)
     * ============================================================ */

    /** 제출 ID 기반 조회 실패 */
    SUBMISSION_NOT_FOUND(HttpStatus.BAD_REQUEST, "SUBMISSION_NOT_FOUND"),

    /** 제출 내역(히스토리) 조회 실패 */
    SUBMISSION_HISTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "SUBMISSION_HISTORY_NOT_FOUND"),

    /** 유저가 제출하려는 알고리즘 ID가 잘못된 경우 */
    INVALID_ALGORITHM(HttpStatus.BAD_REQUEST, "INVALID_ALGORITHM"),

    /* ============================================================
     * REVIEW (코드 리뷰)
     * ============================================================ */

    /** 리뷰 ID 기반 조회 실패 */
    REVIEW_NOT_FOUND(HttpStatus.BAD_REQUEST, "REVIEW_NOT_FOUND"),

    /** 리뷰 좋아요 중복 클릭 */
    REVIEW_LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_LIKE_ALREADY_EXISTS"),

    /* ============================================================
     * ALARM (알림)
     * ============================================================ */

    /** 알림 ID 또는 사용자 알림이 존재하지 않을 때 */
    ALARM_NOT_FOUND(HttpStatus.BAD_REQUEST, "ALARM_NOT_FOUND"),

    /* ============================================================
     * DATABASE / EXTERNAL
     * ============================================================ */

    /** DB 예기치 않은 오류 (FK, Unique, Deadlock 등) */
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR"),

    /** 외부 API 문제 (HTTP 통신 실패, Spotify/Youtube 연동 실패 등) */
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_ERROR"),

    /** 외부 API 타임아웃 */
    TIMEOUT_ERROR(HttpStatus.GATEWAY_TIMEOUT, "TIMEOUT_ERROR"),

    /* ============================================================
     * SERVER
     * ============================================================ */

    /** 처리되지 않은 모든 서버 내부 오류 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");

    private final HttpStatus httpStatusCode;
    private final String errorCode;
}
