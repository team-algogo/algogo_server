package com.ssafy.algogo.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SearchUserRequestDto {

    @NotBlank(message = "search data is nullable")
    private String email;

}
