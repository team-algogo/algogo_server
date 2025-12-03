package com.ssafy.algogo.submission.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubmissionRequestDto {

  @NotNull(message = "프로그램 문제 아이디가 Null입니다.")
  private Long programProblemId;
  @NotBlank(message = "사용 언어가 Null입니다.")
  private String language;
  @NotBlank(message = "제출 코드가 Null입니다.")
  private String code;
  @NotBlank(message = "전략이 Null입니다.")
  @Length(max = 255, message = "전략이 너무 깁니다.")
  private String strategy;
  @NotNull(message = "실행시간이 Null입니다.")
  private Long execTime;
  @NotNull(message = "메모리가 Null입니다.")
  private Long memory;
  @NotNull(message = "성공여부가 Null입니다.")
  private Boolean isSuccess;
  @NotNull(message = "알고리즘이 Null입니다.")
  private List<Long> algorithmList;
}
