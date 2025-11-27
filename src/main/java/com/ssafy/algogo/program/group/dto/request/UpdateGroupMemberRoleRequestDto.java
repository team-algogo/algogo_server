package com.ssafy.algogo.program.group.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateGroupMemberRoleRequestDto {
    @NotNull(message = "role은 필수 값입니다.")
    @Pattern(regexp = "USER|MANAGER", message = "role 값은 'USER' 또는 'MANAGER'이어야 합니다.")
    String role;

}
