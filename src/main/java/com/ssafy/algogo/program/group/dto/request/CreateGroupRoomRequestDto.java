package com.ssafy.algogo.program.group.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateGroupRoomRequestDto {

  @NotBlank(message = "title is required")
  private String title;

  @NotBlank(message = "description is required")
  private String description;

  @NotNull(message = "capacity is required")
  @Min(value = 1, message = "capacity must be at least 1")
  @Max(value = 1000, message = "capacity must be at most 1000")
  private Long capacity;
}
