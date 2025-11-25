package com.ssafy.algogo.problem.controller;

import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/problems")
public class ProblemController {
    private final ProblemService problemService;

    @GetMapping("/{programProblemId}")
    public ResponseEntity<SuccessResponse> getProblem(@PathVariable Long programProblemId){
        return ResponseEntity.ok(
                SuccessResponse
                        .success("Problem 조회 성공", problemService.getProblem(programProblemId)));
    }
}
