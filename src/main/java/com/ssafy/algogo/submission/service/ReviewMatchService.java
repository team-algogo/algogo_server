package com.ssafy.algogo.submission.service;

import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.entity.Submission;
import java.util.List;

public interface ReviewMatchService {

    void matchReviewers(Submission subjectSubmission, List<Algorithm> subjectAlgorithmList,
        int assignCount);
}
