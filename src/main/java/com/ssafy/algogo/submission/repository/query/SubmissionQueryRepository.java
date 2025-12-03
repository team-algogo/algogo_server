package com.ssafy.algogo.submission.repository.query;

import com.ssafy.algogo.submission.dto.request.UserSubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.UserSubmissionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubmissionQueryRepository {

    Page<UserSubmissionResponseDto> findAllUserSubmissionList(Long userId,
        UserSubmissionRequestDto userSubmissionRequestDto, Pageable pageable);

//    List<SubmissionPreviewResponseDto> findHottestSubmissions();

}
