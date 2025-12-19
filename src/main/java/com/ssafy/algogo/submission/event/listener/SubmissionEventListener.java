package com.ssafy.algogo.submission.event.listener;

import com.ssafy.algogo.submission.dto.ReviewRematchTargetQueryDto;
import com.ssafy.algogo.submission.event.SubmissionEvent;
import com.ssafy.algogo.submission.event.SubmissionRematchEvent;
import com.ssafy.algogo.submission.service.ReviewMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SubmissionEventListener {

    private final ReviewMatchService reviewMatchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubmissionEvent(SubmissionEvent submissionEvent) {
        reviewMatchService.matchReviewers(
            submissionEvent.subjectSubmission(),
            submissionEvent.subjectAlgorithmList(),
            submissionEvent.assignCount()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubmissionRematchEvent(SubmissionRematchEvent submissionRematchEvent) {
        for (ReviewRematchTargetQueryDto target : submissionRematchEvent.reviewRematchTargetQueryDtoList()) {
            reviewMatchService.matchReviewers(target.submission(), target.algorithmList(), 1);
        }
    }
}
