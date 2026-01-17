package com.ssafy.algogo.program.problemset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // 빌더용
public class ProblemSetCreateRequestDto {

	@NotBlank
	private String title;
	@NotBlank
	private String description;
	private List<String> categories;  // ⭐ 추가 (null 가능)
}
