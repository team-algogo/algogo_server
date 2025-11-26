package com.ssafy.algogo.program.group.config;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.program.group.entity.GroupRole;
import com.ssafy.algogo.program.group.entity.GroupsUser;
import com.ssafy.algogo.program.group.repository.GroupUserRepository;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class GroupAuthorizationAspect {
  private final GroupUserRepository groupUserRepository;

  @Before("@annotation(groupAuthorize)")  // `@GroupAuthorize`가 붙은 메서드 실행 전에 그룹 권한 체크
  public void checkGroupRole(JoinPoint joinPoint, GroupAuthorize groupAuthorize) throws Throwable {
    // 현재 로그인한 사용자 ID 추출
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
      throw new CustomException("인증 정보가 없습니다.", ErrorCode.UNAUTHORIZED);
    }
    Long userId = details.getUserId();

    // 메서드 파라미터 중 `@GroupId`가 붙은 파라미터에서 `groupId` 추출
    Long groupId = extractGroupId(joinPoint);
    if (groupId == null) {
      throw new IllegalStateException("@GroupAuthorize 사용 시 @GroupId 파라미터가 필요합니다.");
    }

    // 그룹에 대한 사용자의 역할 조회 (DB에서 `groups_user` 테이블)
    GroupsUser groupsUser = groupUserRepository.findByProgramIdAndUserId(groupId, userId)
        .orElseThrow(() -> new CustomException(
            "그룹에 대한 유저 권한을 찾을 수 없습니다.",
            ErrorCode.GROUP_USER_NOT_FOUND
        ));

    GroupRole actualRole = groupsUser.getGroupRole();
    GroupRole requiredRole = groupAuthorize.minRole(); // 메서드에서 요구하는 최소 권한

    // 권한 비교: 현재 사용자의 역할이 필요한 최소 권한 이상인지 확인
    if (!actualRole.hasAtLeast(requiredRole)) {
      throw new CustomException(
          "해당 그룹에 대한 권한이 부족합니다.",
          ErrorCode.GROUP_ACCESS_DENIED
      );
    }
  }

  // 메서드 파라미터 중 @GroupId 달린 파라미터에서 groupId를 추출
  private Long extractGroupId(JoinPoint joinPoint) {
    // 메서드 시그니처 가져오기
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    // 메서드의 파라미터에 달린 어노테이션 정보 가져오기
    Annotation[][] paramAnnotations = method.getParameterAnnotations();
    // 메서드의 실제 파라미터 값들
    Object[] args = joinPoint.getArgs();

    // 모든 파라미터를 순회하며 @GroupId 어노테이션이 있는 파라미터 찾기
    for (int i = 0; i < paramAnnotations.length; i++) {
      // 각 파라미터에 붙은 어노테이션을 하나씩 확인
      for (Annotation annotation : paramAnnotations[i]) {
        // @GroupId 어노테이션이 붙어있는 파라미터를 찾았을 때
        if (annotation instanceof GroupId) {
          Object arg = args[i];
          if (arg instanceof Long l) {
            return l;
          } else {
            throw new IllegalStateException("@GroupId 파라미터는 Long 타입이어야 합니다.");
          }
        }
      }
    }
    return null;
  }
}
