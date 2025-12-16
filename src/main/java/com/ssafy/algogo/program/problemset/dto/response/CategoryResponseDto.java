package com.ssafy.algogo.program.problemset.dto.response;

import com.ssafy.algogo.program.entity.Category;

public record CategoryResponseDto(
    Long id,
    String name
) {

    public static CategoryResponseDto from(Category category) {
        return new CategoryResponseDto(category.getId(), category.getName());
    }
}
