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
public class UpdateUserProfileImageRequestDto {

    @NotBlank(message = "profile image is required")
    private String profileImage;

}
