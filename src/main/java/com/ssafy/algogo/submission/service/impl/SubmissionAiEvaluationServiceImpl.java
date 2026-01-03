package com.ssafy.algogo.submission.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.common.utils.S3Service;
import com.ssafy.algogo.problem.entity.Problem;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.submission.dto.AlgorithmGuideDto;
import com.ssafy.algogo.submission.dto.SubmissionEvaluationContextDto;
import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.submission.repository.AlgorithmRepository;
import com.ssafy.algogo.submission.repository.SubmissionRepository;
import com.ssafy.algogo.submission.service.OpenAiEvaluationService;
import com.ssafy.algogo.submission.service.SubmissionAiEvaluationService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionAiEvaluationServiceImpl implements SubmissionAiEvaluationService {

    private final SubmissionRepository submissionRepository;
    private final AlgorithmRepository algorithmRepository;
    private final OpenAiEvaluationService openAiEvaluationService;
    private final S3Service s3Service;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluateSubmission(Long submissionId) {
        // 1단계: Submission 정보 조회 및 검증
        SubmissionEvaluationContextDto context = collectSubmissionContext(submissionId);

        // 2단계: ElasticSearch에서 알고리즘 가이드 검색 (RAG)
//        List<AlgorithmGuideDto> guides = searchAlgorithmGuides(context);
        List<AlgorithmGuideDto> guides = new ArrayList<>(); // 현재 ElasticSearch 구성이 확정되지 않았으므로 빈 리스트 임시 배치

        // 3단계: 점수 산정 프롬프트 구성
        String scorePrompt = buildScorePrompt(context, guides);

        // 4단계: G-Eval 방식 점수 계산 (logprobs 기반)
        BigDecimal normalizedScore = calculateScore(scorePrompt);

        // 5단계: 평가 이유 생성 프롬프트 구성
        String reasonPrompt = buildReasonPrompt(context, guides, normalizedScore);

        // 6단계: 평가 이유 생성
        String reason = generateReason(reasonPrompt);

        // 7단계: 결과 저장
        saveEvaluationResult(submissionId, normalizedScore, reason);
    }

    /**
     * 1단계: Submission 정보 조회 및 평가 컨텍스트 구성
     */
    private SubmissionEvaluationContextDto collectSubmissionContext(Long submissionId) {

        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new CustomException("제출 ID에 해당하는 정보를 찾을 수 없습니다.",
                ErrorCode.SUBMISSION_NOT_FOUND));

        List<Long> algorithmIds = algorithmRepository.findAllAlgorithmsBySubmissionId(submissionId)
            .stream()
            .map(Algorithm::getId)
            .collect(Collectors.toList());

        ProgramProblem programProblem = submission.getProgramProblem();
        if (programProblem == null) {
            throw new CustomException("프로그램 문제 정보를 찾을 수 없습니다.", ErrorCode.PROBLEM_NOT_FOUND);
        }

        Problem problem = programProblem.getProblem();
        if (problem == null) {
            throw new CustomException("문제 정보를 찾을 수 없습니다.", ErrorCode.PROBLEM_NOT_FOUND);
        }

        // 코드가 S3 URL인 경우 실제 코드 내용 다운로드
        String code = submission.getCode();
        if (code != null && code.startsWith("https://")) {
            try {
                code = s3Service.downloadText(code);
            } catch (Exception e) {
                throw new CustomException("S3의 코드 정보를 가져오는 데 실패했습니다",
                    ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        return SubmissionEvaluationContextDto.builder()
            .submissionId(submission.getId())
            .code(code)
            .language(submission.getLanguage())
            .execTime(submission.getExecTime())
            .memory(submission.getMemory())
            .strategy(submission.getStrategy())
            .isSuccess(submission.getIsSuccess())
            .algorithmIds(algorithmIds)
            .problemId(problem.getId())
            .problemTitle(problem.getTitle())
            .difficultyType(
                problem.getDifficultyType() != null ? problem.getDifficultyType().name() : null)
            .problemLink(problem.getProblemLink())
            .build();
    }

//    /**
//     * 2단계: ElasticSearch에서 알고리즘 가이드 검색 (RAG)
//     */
//    private List<AlgorithmGuideDto> searchAlgorithmGuides(SubmissionEvaluationContextDto context) {
//        if (context.getAlgorithmIds().isEmpty()) {
//            return List.of();
//        }
//
//        List<AlgorithmGuideDto> guides = elasticsearchGuideSearchService.searchAlgorithmGuides(
//            context);
//
//        return guides;
//    }

    /**
     * 3단계: 점수 산정 프롬프트 구성
     */
    private String buildScorePrompt(SubmissionEvaluationContextDto context,
        List<AlgorithmGuideDto> guides) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(
            "당신은 **코딩테스트 평가 전문가**입니다. 코딩테스트 관점에서 제출된 코드의 품질을 객관적이고 정확하게 평가하는 것이 당신의 역할입니다.\n\n");

        prompt.append("## 평가 대상\n");
        prompt.append("다음 코드를 평가해주세요. 이 코드가 평가의 중심입니다.\n\n");

        // 문제 정보
        prompt.append("### 문제 정보\n");
        prompt.append("- 제목: ").append(context.getProblemTitle()).append("\n");
        prompt.append("- 난이도: ")
            .append(context.getDifficultyType() != null ? context.getDifficultyType() : "미지정")
            .append("\n");
        prompt.append("- 언어: ").append(context.getLanguage()).append("\n");
        if (context.getStrategy() != null && !context.getStrategy().isEmpty()) {
            prompt.append("- 제출 전략: ").append(context.getStrategy()).append("\n");
        }
        prompt.append("\n");

        // 실행 결과
        prompt.append("### 실행 결과\n");
        prompt.append("- 성공 여부: ").append(context.getIsSuccess() ? "성공" : "실패").append("\n");
        prompt.append("- 실행 시간: ").append(context.getExecTime()).append("ms\n");
        prompt.append("- 메모리 사용량: ").append(context.getMemory()).append("MB\n");
        prompt.append("\n");

        // 코드
        prompt.append("### 제출 코드\n");
        prompt.append("```").append(context.getLanguage()).append("\n");
        prompt.append(context.getCode()).append("\n");
        prompt.append("```\n\n");

        // 알고리즘 시나리오 (RAG)
        if (guides != null && !guides.isEmpty()) {
            prompt.append("## 평가 참고 자료 (알고리즘 시나리오)\n");
            prompt.append("아래 시나리오들은 **정답 기준이 아닌 평가 참고 기준**입니다. \n");
            prompt.append("이 시나리오들을 참고하여 평가 관점을 확장하되, \n");
            prompt.append("**평가의 중심은 반드시 위의 제출 코드 자체**입니다.\n\n");

            int scenarioCount = Math.min(guides.size(), 5); // 최대 5개
            for (int i = 0; i < scenarioCount; i++) {
                AlgorithmGuideDto guide = guides.get(i);
                prompt.append("### 시나리오 ").append(i + 1);
                if (guide.getScenarioTitle() != null && !guide.getScenarioTitle().isEmpty()) {
                    prompt.append(": ").append(guide.getScenarioTitle());
                } else if (guide.getAlgorithmName() != null) {
                    prompt.append(": ").append(guide.getAlgorithmName());
                }
                prompt.append("\n");

                if (guide.getSituationTags() != null && !guide.getSituationTags().isEmpty()) {
                    prompt.append("- 상황 태그: ").append(String.join(", ", guide.getSituationTags()))
                        .append("\n");
                }
                if (guide.getGuideText() != null && !guide.getGuideText().isEmpty()) {
                    prompt.append("- 가이드: ").append(guide.getGuideText()).append("\n");
                }
                if (guide.getPitfalls() != null && !guide.getPitfalls().isEmpty()) {
                    prompt.append("- 주의사항: ").append(guide.getPitfalls()).append("\n");
                }
                if (guide.getScoringRubricHint() != null && !guide.getScoringRubricHint()
                    .isEmpty()) {
                    prompt.append("- 채점 관점: ").append(guide.getScoringRubricHint()).append("\n");
                }
                prompt.append("\n");
            }
        }

        // 평가 기준
        prompt.append("## 평가 기준 (코딩테스트 관점)\n");
        prompt.append("**중요**: 이 평가는 코딩테스트 관점에서 수행됩니다. \n");
        prompt.append("실제 코딩테스트에서 중요하게 다루는 요소만 평가하고, \n");
        prompt.append("코딩테스트에서 일반적으로 고려하지 않는 요소는 평가에서 배제하세요.\n\n");

        prompt.append("다음 기준에 따라 코드를 평가해주세요:\n\n");

        prompt.append("1. **정확성 (가장 중요)**: 코드가 문제의 요구사항을 정확히 해결하는가? \n");
        prompt.append("   실행 결과의 성공 여부를 반드시 고려하세요. 실패한 코드는 정확성 점수가 낮아야 합니다.\n");
        prompt.append("2. **효율성 (매우 중요)**: 시간복잡도와 공간복잡도가 적절한가? \n");
        prompt.append("   실행 시간과 메모리 사용량을 반드시 고려하세요. \n");
        prompt.append("   과도한 실행 시간이나 메모리 사용은 효율성 점수를 낮춥니다.\n");
        prompt.append("3. **알고리즘 적절성**: 문제 해결을 위한 알고리즘 선택이 적절한가? \n");
        prompt.append("   시간복잡도와 공간복잡도 관점에서 최적의 알고리즘을 사용했는가?\n");
        prompt.append("4. **코드 구조**: 코드 구조가 명확하고 이해하기 쉬운가? \n");
        prompt.append("   변수명과 함수명이 의미를 잘 전달하는가?\n\n");

        prompt.append("**평가에서 배제할 요소**:\n");
        prompt.append("- 예외 처리: 코딩테스트에서는 입력이 항상 유효하다고 가정하므로, \n");
        prompt.append("  예외 처리 코드의 유무나 완성도는 평가에 영향을 주지 않습니다.\n");
        prompt.append("- 주석: 코딩테스트에서는 주석이 필수적이지 않으므로, \n");
        prompt.append("  주석의 유무나 품질은 평가에 영향을 주지 않습니다.\n");
        prompt.append("- 코드 스타일 세부사항: 들여쓰기, 공백 등 스타일 세부사항은 평가에 영향을 주지 않습니다.\n\n");

        // 출력 형식
        prompt.append("## 출력 형식\n");
        prompt.append("**반드시 1~5 중 하나의 숫자만 출력하세요. 다른 텍스트, 설명, 공백, 줄바꿈 등은 절대 포함하지 마세요.**\n\n");

        prompt.append("선택지는 반드시 아래 중 하나입니다.\n\n");

        prompt.append("1\n");
        prompt.append("2\n");
        prompt.append("3\n");
        prompt.append("4\n");
        prompt.append("5\n\n");

        prompt.append("**중요**: \"점수:\", \"점수는\", \"4점\" 등과 같은 추가 텍스트 없이 숫자만 출력해야 합니다.\n\n");

        prompt.append("**중요**:\n");
        prompt.append("- JSON 형식으로 출력하지 마세요\n");
        prompt.append("- 설명이나 근거를 포함하지 마세요\n");
        prompt.append("- 오직 숫자 하나만 출력하세요\n");
        prompt.append("- 1은 가장 낮은 점수, 5는 가장 높은 점수입니다\n");

        return prompt.toString();
    }

    /**
     * 4단계: G-Eval 방식 점수 계산 (logprobs 기반)
     */
    private BigDecimal calculateScore(String scorePrompt) {
        BigDecimal normalizedScore = openAiEvaluationService.evaluateScoreWithLogprobs(scorePrompt);

        if (normalizedScore == null) {
            throw new CustomException(
                "점수 계산 결과가 null입니다.",
                ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        if (normalizedScore.compareTo(BigDecimal.ZERO) < 0 ||
            normalizedScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            normalizedScore = normalizedScore.max(BigDecimal.ZERO)
                .min(BigDecimal.valueOf(100));
        }

        return normalizedScore;
    }

    /**
     * 5단계: 평가 이유 생성 프롬프트 구성
     */
    private String buildReasonPrompt(SubmissionEvaluationContextDto context,
        List<AlgorithmGuideDto> guides, BigDecimal score) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 **코딩테스트 평가 전문가**입니다. \n");
        prompt.append("코딩테스트 관점에서 이미 계산된 점수에 대한 평가 근거를 명확하고 구체적으로 설명하는 것이 당신의 역할입니다.\n\n");

        // 평가 대상 정보
        prompt.append("## 평가 대상 정보\n");
        prompt.append("- 문제 제목: ").append(context.getProblemTitle()).append("\n");
        prompt.append("- 난이도: ")
            .append(context.getDifficultyType() != null ? context.getDifficultyType() : "미지정")
            .append("\n");
        prompt.append("- 언어: ").append(context.getLanguage()).append("\n");
        if (context.getStrategy() != null && !context.getStrategy().isEmpty()) {
            prompt.append("- 제출 전략: ").append(context.getStrategy()).append("\n");
        }
        prompt.append("\n");

        // 실행 결과
        prompt.append("### 실행 결과\n");
        prompt.append("- 성공 여부: ").append(context.getIsSuccess() ? "성공" : "실패").append("\n");
        prompt.append("- 실행 시간: ").append(context.getExecTime()).append("ms\n");
        prompt.append("- 메모리 사용량: ").append(context.getMemory()).append("MB\n");
        prompt.append("\n");

        // 제출 코드
        prompt.append("## 제출 코드\n");
        prompt.append("```").append(context.getLanguage()).append("\n");
        prompt.append(context.getCode()).append("\n");
        prompt.append("```\n\n");

        // 계산된 점수
        prompt.append("## 계산된 점수\n");
        prompt.append("이 코드에 대한 평가 점수는 **").append(score)
            .append("점 (0~100점 만점)**입니다.\n\n");

        // 평가 관점 참고 자료
        if (guides != null && !guides.isEmpty()) {
            prompt.append("## 평가 관점 참고 자료\n");
            prompt.append("아래 시나리오들은 평가 관점을 참고하기 위한 자료입니다. \n");
            prompt.append("이 시나리오들을 바탕으로 평가 근거를 설명하되, \n");
            prompt.append("**반드시 위의 제출 코드의 구체적인 부분과 연결하여 설명**해야 합니다.\n\n");

            int scenarioCount = Math.min(guides.size(), 5); // 최대 5개
            for (int i = 0; i < scenarioCount; i++) {
                AlgorithmGuideDto guide = guides.get(i);
                prompt.append("### 참고 시나리오 ").append(i + 1);
                if (guide.getScenarioTitle() != null && !guide.getScenarioTitle().isEmpty()) {
                    prompt.append(": ").append(guide.getScenarioTitle());
                } else if (guide.getAlgorithmName() != null) {
                    prompt.append(": ").append(guide.getAlgorithmName());
                }
                prompt.append("\n");

                if (guide.getGuideText() != null && !guide.getGuideText().isEmpty()) {
                    prompt.append("- 가이드: ").append(guide.getGuideText()).append("\n");
                }
                if (guide.getPitfalls() != null && !guide.getPitfalls().isEmpty()) {
                    prompt.append("- 주의사항: ").append(guide.getPitfalls()).append("\n");
                }
                if (guide.getScoringRubricHint() != null && !guide.getScoringRubricHint()
                    .isEmpty()) {
                    prompt.append("- 채점 관점: ").append(guide.getScoringRubricHint()).append("\n");
                }
                prompt.append("\n");
            }
        }

        // 평가 근거 작성 지침
        prompt.append("## 평가 근거 작성 지침 (코딩테스트 관점)\n");
        prompt.append("**중요**: 이 평가는 코딩테스트 관점에서 수행됩니다. \n");
        prompt.append("실제 코딩테스트에서 중요하게 다루는 요소만 평가하고, \n");
        prompt.append("코딩테스트에서 일반적으로 고려하지 않는 요소는 평가에서 배제하세요.\n\n");

        prompt.append("위 점수(").append(score).append("점)가 나온 이유를 다음 기준에 따라 설명해주세요:\n\n");

        prompt.append("1. **정확성**: 실행 결과의 성공 여부를 반드시 언급하세요. \n");
        prompt.append("   실패한 경우 왜 실패했는지, 성공한 경우 정확히 해결했는지 설명하세요.\n");
        prompt.append("2. **효율성**: 실행 시간과 메모리 사용량을 반드시 언급하세요. \n");
        prompt.append("   시간복잡도와 공간복잡도를 분석하고, 실행 결과와 연결하여 설명하세요.\n");
        prompt.append("3. **알고리즘 적절성**: 사용된 알고리즘이 문제 해결에 적절한지, \n");
        prompt.append("   더 효율적인 알고리즘이 있는지 설명하세요.\n");
        prompt.append("4. **코드의 구체적인 부분을 인용**하여 설명하세요. \n");
        prompt.append("   라인 번호, 함수명, 변수명 등을 명시하세요.\n");
        prompt.append("5. **개선 가능한 부분**이 있다면 언급하세요. \n");
        prompt.append("   단, 코딩테스트 관점에서 중요한 개선점만 언급하세요.\n\n");

        prompt.append("**평가에서 배제할 요소 (근거에 포함하지 마세요)**:\n");
        prompt.append("- 예외 처리: 예외 처리 코드의 유무나 완성도는 언급하지 마세요.\n");
        prompt.append("- 주석: 주석의 유무나 품질은 언급하지 마세요.\n");
        prompt.append("- 코드 스타일 세부사항: 들여쓰기, 공백 등은 언급하지 마세요.\n\n");

        // 출력 형식
        prompt.append("## 출력 형식\n");
        prompt.append("**반드시 다음 JSON 형식으로만 출력하세요.**\n\n");

        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"reason\": \"점수에 대한 평가 근거를 코드의 구체적인 부분과 연결하여 설명하세요.\"\n");
        prompt.append("}\n");
        prompt.append("```\n\n");

        prompt.append("**중요**:\n");
        prompt.append("- 반드시 유효한 JSON 형식으로 출력하세요\n");
        prompt.append("- reason 필드는 문자열이며, 점수에 대한 평가 근거를 포함합니다\n");
        prompt.append("- 코드의 구체적인 부분(라인 번호, 함수명, 변수명 등)을 인용하여 설명하세요\n");
        prompt.append("- 평가 관점 참고 자료를 활용하되, 코드와 직접 연결하여 설명하세요\n");

        return prompt.toString();
    }

    /**
     * 6단계: 평가 이유 생성
     */
    private String generateReason(String reasonPrompt) {
        String reason = openAiEvaluationService.generateEvaluationReason(reasonPrompt);

        if (reason == null || reason.trim().isEmpty()) {
            throw new CustomException(
                "평가 이유가 비어있습니다.",
                ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        return reason;
    }

    /**
     * 7단계: 평가 결과 저장
     */
    private void saveEvaluationResult(Long submissionId, BigDecimal score, String reason) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new CustomException(
                "제출 정보를 찾을 수 없습니다.",
                ErrorCode.SUBMISSION_NOT_FOUND
            ));

        submission.updateAiEvaluation(score, reason);
        submissionRepository.save(submission);
        submissionRepository.flush();
    }
}
