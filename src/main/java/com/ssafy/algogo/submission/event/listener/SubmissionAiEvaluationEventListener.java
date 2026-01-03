package com.ssafy.algogo.submission.event.listener;

import com.ssafy.algogo.submission.event.SubmissionAiEvaluationEvent;
import com.ssafy.algogo.submission.service.SubmissionAiEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SubmissionAiEvaluationEventListener {

    private final SubmissionAiEvaluationService submissionAiEvaluationService;

    @Async("aiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubmissionAiEvaluationEvent(
        SubmissionAiEvaluationEvent submissionAiEvaluationEvent) {
        submissionAiEvaluationService.evaluateSubmission(
            submissionAiEvaluationEvent.submissionId());
    }
}
