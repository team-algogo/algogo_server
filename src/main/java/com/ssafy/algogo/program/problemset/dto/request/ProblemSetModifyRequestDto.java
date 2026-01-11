package com.ssafy.algogo.program.problemset.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProblemSetModifyRequestDto {

	private String title;
	private String description;
	private MultipartFile thumbnail;  // 선택
	private List<String> categories;  // 선택 (null 시 기존 삭제 안함)
}
