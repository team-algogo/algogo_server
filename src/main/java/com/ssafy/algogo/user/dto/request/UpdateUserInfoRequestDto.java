package com.ssafy.algogo.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UpdateUserInfoRequestDto {

    @NotBlank(message = "description is required")
    private String description;

    @NotBlank(message = "nickname is required")
    private String nickname;
}
