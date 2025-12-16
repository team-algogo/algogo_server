package com.ssafy.algogo.program.problemset.dto.response;

import java.util.List;

public record CategoryListResponseDto(
    List<CategoryResponseDto> categoryList
) {

}
