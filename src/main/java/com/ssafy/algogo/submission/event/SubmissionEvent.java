package com.ssafy.algogo.submission.event;

import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.entity.Submission;
import java.util.List;

public record SubmissionEvent(
    Submission subjectSubmission,
    List<Algorithm> subjectAlgorithmList,
    int assignCount
) {

}
