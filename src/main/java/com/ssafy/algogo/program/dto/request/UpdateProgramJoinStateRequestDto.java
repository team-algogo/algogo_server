package com.ssafy.algogo.program.dto.request;

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
public class UpdateProgramJoinStateRequestDto {

  @NotNull(message = "isAccepted는 필수 값입니다.")
  @Pattern(regexp = "ACCEPTED|DENIED", message = "isAccepted 값은 'ACCEPTED' 또는 'DENIED'이어야 합니다.")
  String isAccepted;

}
