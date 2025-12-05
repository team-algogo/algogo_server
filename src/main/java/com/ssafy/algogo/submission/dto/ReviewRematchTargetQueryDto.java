package com.ssafy.algogo.submission.dto;

import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.entity.Submission;
import java.util.List;

public record ReviewRematchTargetQueryDto(
    Submission submission,
    List<Algorithm> algorithmList
) {

}
