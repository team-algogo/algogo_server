package com.ssafy.algogo.submission.service;

import java.math.BigDecimal;

public interface OpenAiEvaluationService {

    BigDecimal evaluateScoreWithLogprobs(String scorePrompt);

    String generateEvaluationReason(String reasonPrompt);
}

