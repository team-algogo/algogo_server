package com.ssafy.algogo.program.problemset.controller;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.problem.dto.request.ProgramProblemCreateRequestDto;
import com.ssafy.algogo.problem.dto.request.ProgramProblemDeleteRequestDto;
import com.ssafy.algogo.problem.service.impl.ProgramProblemServiceImpl;

import com.ssafy.algogo.program.problemset.dto.request.ProblemSetCreateRequestDto;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetModifyRequestDto;
import com.ssafy.algogo.program.problemset.dto.request.ProgramProblemsDeleteRequestDto;
import com.ssafy.algogo.program.problemset.dto.response.MyProblemSetListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetProblemsPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import com.ssafy.algogo.program.problemset.service.ProblemSetService;
import com.ssafy.algogo.user.entity.UserRole;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/problem-sets")
public class ProblemSetController {

	private final ProblemSetService problemSetService;
	private final ProgramProblemServiceImpl programProblemServiceImpl;

	/**
	 * 자율 문제집 리스트 조회 * 동적 쿼리. //@return 성공시 반환할거 적을거임 //@throws 예외 적을거임
	 */
	@GetMapping("/lists")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse getProblemSetList(
		@RequestParam String keyWord,
		@RequestParam String category,
		@RequestParam String sortBy,
		@RequestParam String sortDirection
	) {

		List<ProblemSetResponseDto> data = problemSetService.getProblemSetList(
			keyWord, category, sortBy, sortDirection);

		return new SuccessResponse("자율 문제집 리스트 조회를 성공했습니다.", data);
	}


	// 자율 문제집 조회
	@GetMapping("/{program_id}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse getProblemSet(@PathVariable Long program_id) {
		ProblemSetResponseDto data = problemSetService.getProblemSet(program_id);
		return new SuccessResponse("자율 문제집 조회를 성공했습니다.", data);
	}

	// 자율 문제집 생성
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("")
	@ResponseStatus(HttpStatus.CREATED)
	public SuccessResponse createProblemSet(
		@RequestBody @Valid ProblemSetCreateRequestDto createRequestDto
	) {
		ProblemSetResponseDto data = problemSetService.createProblemSet(
			createRequestDto);
		return new SuccessResponse("문제집 생성을 성공했습니다.", data);
	}

	// 자율 문제집 수정
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{program_id}")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse modifyProblemSet(@PathVariable Long program_id,
		@RequestBody @Valid ProblemSetModifyRequestDto problemSetModifyRequestDto) {

		ProblemSetResponseDto data = problemSetService.modifyProblemSet(
			program_id, problemSetModifyRequestDto);

		return new SuccessResponse("문제집 수정을 성공했습니다.", data);
	}

	// 자율 문제집 삭제
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{program_id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public SuccessResponse deleteProblemSet(@PathVariable Long program_id) {

		problemSetService.deleteProblemSet(program_id);

		return new SuccessResponse("자율 문제집 삭제를 성공했습니다.", null);
	}

	@GetMapping("/{program_id}/problems")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse getProblemsByProblemSetId(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long program_id,
		@RequestParam(defaultValue = "id") String sortBy,
		@RequestParam(defaultValue = "asc") String sortDirection,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "0") int page
	) {
		boolean isLogined = (user != null);

		ProblemSetProblemsPageResponseDto data =
			problemSetService.getProgramProblemsPage(
				program_id, isLogined, sortBy, sortDirection, size, page
			);

		return new SuccessResponse("문제집 문제 리스트 조회에 성공했습니다.", data);
	}


	// 문제집 요소 추가(문제추가)
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{program_id}/problems")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public SuccessResponse commonProblemCreate(
		@PathVariable Long program_id,
		@RequestBody ProgramProblemCreateRequestDto programProblemCreateRequestDto) {

		programProblemServiceImpl.createProgramProblem(program_id, programProblemCreateRequestDto);

		return new SuccessResponse("문제집 요소 추가(문제)를 성공했습니다.", null);
	}


	// 	문제집 요소 제거(문제제거)
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{program_id}/problems")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public SuccessResponse DeleteProblemSetByProblem(@PathVariable Long program_id,
		@RequestBody @Valid ProgramProblemDeleteRequestDto programProblemsDeleteRequestDto) {

		programProblemServiceImpl.deleteProgramProblem(program_id,
			programProblemsDeleteRequestDto);

		return new SuccessResponse("문제집 요소 제거를 성공했습니다.", null);
	}

	// 내가 참여한 문제집 조회
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@GetMapping("/me")
	@ResponseStatus(HttpStatus.OK)
	public SuccessResponse getJoinMe(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

		if (customUserDetails == null) {
			throw new CustomException("인증 정보가 없습니다.", ErrorCode.UNAUTHORIZED);
		}

		Long userId = customUserDetails.getUserId();

		MyProblemSetListResponseDto data =
			problemSetService.getMeJoinProblemSet(userId);

		return new SuccessResponse("내가 참여한 문제집 조회에 성공했습니다.", data);
	}

	/*@GetMapping("/debug/me") // 유저권한 확인용
	public String debug(Authentication authentication) {

		System.out.println(authentication.getAuthorities());
		return "ok";
	}*/
}
