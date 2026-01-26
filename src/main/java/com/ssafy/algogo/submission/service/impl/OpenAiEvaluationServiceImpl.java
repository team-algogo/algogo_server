package com.ssafy.algogo.submission.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.common.config.OpenAIConfig;
import com.ssafy.algogo.submission.service.OpenAiEvaluationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * OpenAI 평가 서비스 구현체
 * <p>
 * G-Eval(logprobs 기반 평가) 방식으로 점수를 계산합니다.
 * <p>
 * 동작 방식: 1. 점수 산정: logprobs를 포함한 API 호출 → logprobs 기반 점수 계산 2. 이유 생성: 일반 API 호출 → JSON 파싱하여 reason
 * 추출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiEvaluationServiceImpl implements OpenAiEvaluationService {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    private static final int MIN_NORMALIZED_SCORE = 0;
    private static final int MAX_NORMALIZED_SCORE = 100;
    private static final Pattern SCORE_PATTERN = Pattern.compile("점수:\\s*(\\d+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b([1-5])\\b");

    private final OpenAIConfig openAIConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BigDecimal evaluateScoreWithLogprobs(String scorePrompt) {

        try {
            Map<String, Object> requestBody = buildScoreRequest(scorePrompt);

            String responseBody = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null) {
                throw new CustomException("OpenAI API 응답이 null입니다.",
                        ErrorCode.INTERNAL_SERVER_ERROR);
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody);

            BigDecimal rawScore = calculateScoreFromLogprobs(jsonNode);
            BigDecimal normalizedScore = normalizeScore(rawScore);

            return normalizedScore;

        } catch (WebClientResponseException e) {
            log.error("[G-Eval] OpenAI API HTTP 오류 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(
                    "OpenAI API 호출 실패: " + e.getMessage(),
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            log.error("[G-Eval] 점수 산정 오류", e);
            throw new CustomException(
                    "점수 산정 중 오류 발생: " + e.getMessage(),
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public String generateEvaluationReason(String reasonPrompt) {
        try {
            Map<String, Object> requestBody = buildReasonRequest(reasonPrompt);

            String responseBody = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null) {
                log.error("[평가 이유 생성] OpenAI API 응답이 null입니다.");
                return "AI 평가 이유를 생성하지 못했습니다. (응답 없음)";
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String response = extractContentFromResponse(jsonNode);

            String reason = parseReasonFromJson(response);

            return reason;

        } catch (WebClientResponseException e) {
            log.error("[평가 이유 생성] OpenAI API HTTP 오류 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return "AI 평가 이유를 생성하지 못했습니다. (API 오류: " + e.getStatusCode() + ")";
        } catch (Exception e) {
            log.error("[평가 이유 생성] 오류", e);
            return "AI 평가 이유를 생성하지 못했습니다. (오류: " + e.getMessage() + ")";
        }
    }

    /**
     * 점수 산정용 요청 본문 구성
     * <p>
     * G-Eval 방식에 맞춰 단일 토큰(1~5 중 하나)만 출력하도록 제한합니다. - max_tokens=1: 단일 토큰만 생성하여 categorical 분포로 해석
     * 가능하게 함 - temperature=0.0: deterministic 출력 - logprobs=true: 확률 계산을 위한 logprobs 포함
     */
    private Map<String, Object> buildScoreRequest(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAIConfig.getModel());

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "당신은 알고리즘 코드 평가 전문가입니다.");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.0);
        requestBody.put("max_tokens", 1);
        requestBody.put("logprobs", true);
        requestBody.put("top_logprobs", 10);
        return requestBody;
    }

    /**
     * 평가 이유 생성용 요청 본문 구성
     */
    private Map<String, Object> buildReasonRequest(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAIConfig.getModel());

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "당신은 알고리즘 코드 평가 전문가입니다.");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.2);
        requestBody.put("max_tokens", 1000);
        return requestBody;
    }

    /**
     * logprobs 기반 점수 계산 (G-Eval 방식)
     * <p>
     * G-Eval의 확률 해석 방식: 1. 첫 번째 토큰 위치의 logprobs만 사용 (단일 categorical 분포로 해석) 2. 동일 점수 토큰의 확률은
     * 누적(sum) - probability mass 개념 3. softmax 정규화를 통해 P(score = i) 계산 4. 기대값 계산: rawScore = Σ(i ×
     * P(i)) for i in [1, 5]
     */
    private BigDecimal calculateScoreFromLogprobs(JsonNode jsonNode) {
        JsonNode choices = jsonNode.get("choices");
        if (choices == null || !choices.isArray() || choices.size() == 0) {
            log.warn("[G-Eval] choices가 없거나 비어있음. 응답에서 점수 추출 불가");
            throw new CustomException("응답에 choices가 없습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        JsonNode choice = choices.get(0);
        JsonNode logprobs = choice.get("logprobs");

        if (logprobs == null || logprobs.get("content") == null) {
            log.warn("[G-Eval] logprobs가 없음. 응답 텍스트에서 점수 추출 시도");
            JsonNode message = choice.get("message");
            if (message != null) {
                return extractScoreFromText(message.get("content").asText());
            }
            throw new CustomException("logprobs 정보가 없습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 첫 번째 토큰 위치의 logprobs만 사용 (단일 categorical 분포)
        JsonNode content = logprobs.get("content");
        if (!content.isArray() || content.size() == 0) {
            log.warn("[G-Eval] logprobs content가 비어있음. 응답 텍스트에서 점수 추출 시도");
            JsonNode message = choice.get("message");
            if (message != null) {
                return extractScoreFromText(message.get("content").asText());
            }
            throw new CustomException("logprobs content가 비어있습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 첫 번째 토큰 위치의 top_logprobs 추출
        JsonNode firstTokenLogprob = content.get(0);
        JsonNode topLogprobs = firstTokenLogprob.get("top_logprobs");

        if (topLogprobs == null || !topLogprobs.isArray() || topLogprobs.size() == 0) {
            log.warn("[G-Eval] top_logprobs가 없음. 응답 텍스트에서 점수 추출 시도");
            JsonNode message = choice.get("message");
            if (message != null) {
                return extractScoreFromText(message.get("content").asText());
            }
            throw new CustomException("top_logprobs 정보가 없습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 각 점수(1~5)에 대한 확률 누적 (G-Eval: 동일 점수 토큰의 확률은 sum)
        // 수치 안정성을 위해 max_logprob를 먼저 찾고, exp 변환 시 사용
        Map<Integer, Double> scoreLogprobs = new HashMap<>();
        double maxLogprob = Double.NEGATIVE_INFINITY;

        // 1단계: 각 토큰의 logprob를 점수별로 수집
        for (JsonNode topLogprob : topLogprobs) {
            String token = topLogprob.get("token").asText().trim();
            try {
                int score = Integer.parseInt(token);
                if (score >= MIN_SCORE && score <= MAX_SCORE) {
                    double logprob = topLogprob.get("logprob").asDouble();
                    // 동일 점수 토큰의 logprob를 수집 (나중에 log-sum-exp로 누적)
                    if (scoreLogprobs.containsKey(score)) {
                        // log-sum-exp: log(exp(a) + exp(b)) = max(a,b) + log(1 + exp(-|a-b|))
                        double existing = scoreLogprobs.get(score);
                        scoreLogprobs.put(score, Math.max(existing, logprob) +
                                Math.log1p(Math.exp(-Math.abs(existing - logprob))));
                    } else {
                        scoreLogprobs.put(score, logprob);
                    }
                    maxLogprob = Math.max(maxLogprob, scoreLogprobs.get(score));
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        if (scoreLogprobs.isEmpty()) {
            log.warn("[G-Eval] logprobs에서 숫자 토큰(1~5)을 찾을 수 없음. 응답 텍스트에서 점수 추출 시도");
            JsonNode message = choice.get("message");
            if (message != null) {
                return extractScoreFromText(message.get("content").asText());
            }
            throw new CustomException("점수를 계산할 수 없습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 2단계: Softmax 정규화로 P(score = i) 계산
        // 수치 안정성을 위해 max_logprob를 빼서 계산
        Map<Integer, Double> probabilities = new HashMap<>();
        double sumExp = 0.0;

        for (Map.Entry<Integer, Double> entry : scoreLogprobs.entrySet()) {
            double normalizedLogprob = entry.getValue() - maxLogprob;
            double prob = Math.exp(normalizedLogprob);
            probabilities.put(entry.getKey(), prob);
            sumExp += prob;
        }

        // 3단계: 확률 정규화 (합이 1이 되도록) - categorical 분포 완성
        if (sumExp > 0) {
            for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
                probabilities.put(entry.getKey(), entry.getValue() / sumExp);
            }
        } else {
            throw new CustomException("확률 합이 0입니다. logprobs 파싱 오류 가능성.",
                    ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 기대값 계산: rawScore = Σ(i × P(i))
        double expectedValue = 0.0;
        for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
            expectedValue += entry.getKey() * entry.getValue();
        }

        BigDecimal rawScore = BigDecimal.valueOf(expectedValue)
                .setScale(2, RoundingMode.HALF_UP);

        log.info(
                "[G-Eval] logprobs 기반 점수 계산 - scoreLogprobs: {}, probabilities: {}, expectedValue: {}, rawScore: {}",
                scoreLogprobs, probabilities, expectedValue, rawScore);

        return rawScore;
    }

    /**
     * 응답 텍스트에서 점수 추출 (fallback)
     * <p>
     * logprobs가 없는 경우 응답 텍스트에서 점수를 추출합니다.
     */
    private BigDecimal extractScoreFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new CustomException("응답 텍스트가 비어있습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Matcher scoreMatcher = SCORE_PATTERN.matcher(text);
        if (scoreMatcher.find()) {
            int score = Integer.parseInt(scoreMatcher.group(1));
            if (score >= MIN_SCORE && score <= MAX_SCORE) {
                return BigDecimal.valueOf(score);
            }
        }

        Matcher numberMatcher = NUMBER_PATTERN.matcher(text);
        if (numberMatcher.find()) {
            int score = Integer.parseInt(numberMatcher.group(1));
            if (score >= MIN_SCORE && score <= MAX_SCORE) {
                return BigDecimal.valueOf(score);
            }
        }

        throw new CustomException("응답에서 점수를 추출할 수 없습니다: " + text, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 점수 정규화 (1~5 스케일 → 0~100 스케일)
     */
    private BigDecimal normalizeScore(BigDecimal rawScore) {
        if (rawScore.compareTo(BigDecimal.valueOf(MIN_SCORE)) < 0) {
            rawScore = BigDecimal.valueOf(MIN_SCORE);
        }
        if (rawScore.compareTo(BigDecimal.valueOf(MAX_SCORE)) > 0) {
            rawScore = BigDecimal.valueOf(MAX_SCORE);
        }

        BigDecimal normalized = rawScore
                .subtract(BigDecimal.valueOf(MIN_SCORE))
                .divide(BigDecimal.valueOf(MAX_SCORE - MIN_SCORE), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(MAX_NORMALIZED_SCORE - MIN_NORMALIZED_SCORE))
                .add(BigDecimal.valueOf(MIN_NORMALIZED_SCORE));

        return normalized.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * OpenAI API 응답에서 content 추출
     */
    private String extractContentFromResponse(JsonNode jsonNode) {
        JsonNode choices = jsonNode.get("choices");
        if (choices == null || !choices.isArray() || choices.size() == 0) {
            throw new CustomException("응답에 choices가 없습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        JsonNode choice = choices.get(0);
        JsonNode message = choice.get("message");
        if (message == null) {
            throw new CustomException("응답에 message가 없습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        JsonNode content = message.get("content");
        if (content == null) {
            throw new CustomException("응답에 content가 없습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return content.asText();
    }

    /**
     * JSON 응답에서 reason 추출 (개선된 버전)
     * <p>
     * 1. Markdown 코드 블록(```json ... ```) 제거
     * 2. JSON 파싱 시도
     * 3. 실패 시 부분 텍스트 추출 또는 전체 텍스트 반환
     */
    private String parseReasonFromJson(String responseText) {
        if (responseText == null || responseText.trim().isEmpty()) {
            log.warn("[평가 이유 생성] 응답 텍스트가 비어있습니다.");
            return "AI 평가 이유를 생성하지 못했습니다.";
        }

        try {
            // Markdown 코드 블록 제거
            String cleanedText = responseText.trim();

            // ```json으로 시작하는 경우 제거
            if (cleanedText.startsWith("```json")) {
                cleanedText = cleanedText.substring(7); // "```json" 길이만큼 제거
            } else if (cleanedText.startsWith("```")) {
                cleanedText = cleanedText.substring(3); // "```" 길이만큼 제거
            }

            // ```으로 끝나는 경우 제거
            if (cleanedText.endsWith("```")) {
                cleanedText = cleanedText.substring(0, cleanedText.length() - 3);
            }

            cleanedText = cleanedText.trim();

            // JSON 파싱 시도
            int jsonStart = cleanedText.indexOf("{");
            int jsonEnd = cleanedText.lastIndexOf("}") + 1;

            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = cleanedText.substring(jsonStart, jsonEnd);

                try {
                    JsonNode jsonNode = objectMapper.readTree(jsonStr);
                    JsonNode reasonNode = jsonNode.get("reason");

                    if (reasonNode != null && !reasonNode.asText().trim().isEmpty()) {
                        return reasonNode.asText();
                    }
                } catch (Exception e) {
                    log.warn("[평가 이유 생성] 완전한 JSON 파싱 실패, 부분 텍스트 추출 시도 - {}", e.getMessage());
                }
            }

            // JSON 파싱 실패 시 - 부분 reason 필드 추출 시도
            // "reason": "..." 패턴으로 reason 값만 추출
            int reasonStart = cleanedText.indexOf("\"reason\"");
            if (reasonStart >= 0) {
                int colonIndex = cleanedText.indexOf(":", reasonStart);
                if (colonIndex >= 0) {
                    int valueStart = cleanedText.indexOf("\"", colonIndex);
                    if (valueStart >= 0) {
                        valueStart++; // 따옴표 다음부터 시작

                        // 닫는 따옴표 찾기 (이스케이프된 따옴표는 무시)
                        int valueEnd = valueStart;
                        boolean escaped = false;
                        while (valueEnd < cleanedText.length()) {
                            char c = cleanedText.charAt(valueEnd);
                            if (escaped) {
                                escaped = false;
                            } else if (c == '\\') {
                                escaped = true;
                            } else if (c == '"') {
                                // 닫는 따옴표 발견
                                String extractedReason = cleanedText.substring(valueStart, valueEnd);
                                // 이스케이프 문자 처리
                                extractedReason = extractedReason.replace("\\n", "\n")
                                        .replace("\\\"", "\"")
                                        .replace("\\\\", "\\");

                                if (!extractedReason.trim().isEmpty()) {
                                    log.info("[평가 이유 생성] 부분 reason 필드 추출 성공");
                                    return extractedReason;
                                }
                                break;
                            }
                            valueEnd++;
                        }

                        // 닫는 따옴표를 찾지 못한 경우 (잘린 경우) - 끝까지 추출
                        if (valueEnd >= cleanedText.length() || cleanedText.charAt(valueEnd) != '"') {
                            String partialReason = cleanedText.substring(valueStart);
                            // 마지막 완전한 문장까지만 추출
                            int lastPeriod = partialReason.lastIndexOf(".");
                            int lastNewline = partialReason.lastIndexOf("\\n");
                            int cutPoint = Math.max(lastPeriod, lastNewline);

                            if (cutPoint > 0) {
                                partialReason = partialReason.substring(0, cutPoint + 1);
                            }

                            partialReason = partialReason.replace("\\n", "\n")
                                    .replace("\\\"", "\"")
                                    .replace("\\\\", "\\");

                            if (!partialReason.trim().isEmpty()) {
                                log.warn("[평가 이유 생성] JSON이 잘려있어 부분 텍스트 반환");
                                return partialReason + "\n\n(평가 내용이 길어 일부만 표시됩니다)";
                            }
                        }
                    }
                }
            }

            // 모든 파싱 실패 - 원본 텍스트 반환
            log.warn("[평가 이유 생성] JSON 파싱 및 부분 추출 실패, 원본 텍스트 반환");
            return cleanedText.length() > 1000
                    ? cleanedText.substring(0, 1000) + "\n\n(평가 내용이 길어 일부만 표시됩니다)"
                    : cleanedText;

        } catch (Exception e) {
            log.error("[평가 이유 생성] 예외 발생 - responseText: {}", responseText, e);
            // 예외 발생 시에도 원본 텍스트 반환
            return responseText.length() > 1000
                    ? responseText.substring(0, 1000) + "\n\n(평가 내용이 길어 일부만 표시됩니다)"
                    : responseText;
        }
    }
}

