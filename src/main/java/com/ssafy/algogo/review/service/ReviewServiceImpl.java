package com.ssafy.algogo.review.service;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.review.dto.CodeReviewCreateRequestDto;
import com.ssafy.algogo.review.dto.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.entity.Review;
import com.ssafy.algogo.review.repository.ReviewRepository;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.submission.repository.SubmissionRepository;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;

  @Override
  public CodeReviewTreeResponseDto codeReviewCreate(CodeReviewCreateRequestDto reviewRequest, Long userId) {

    Submission submission = submissionRepository.findById(reviewRequest.getSubmissionId())
        .orElseThrow(() -> new CustomException("submission ID에 해당하는 데이터가 DB에 없습니다.",
            ErrorCode.SUBMISSION_NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException("user ID에 해당하는 데이터가 DB에 없습니다.", ErrorCode.USER_NOT_FOUND));

    Review parentReview = null;
    if(reviewRequest.getParentReviewId() != null) {
      parentReview = reviewRepository.findById(reviewRequest.getParentReviewId())
          .orElseThrow(() -> new CustomException("parentReview ID에 해당하는 데이터가 DB에 없습니다.",
              ErrorCode.REVIEW_NOT_FOUND));
    }

    Review newReview = Review.builder()
        .codeLine(reviewRequest.getCodeLine())
        .likeCount(0L)
        .parentReview(parentReview)
        .submission(submission)
        .user(user)
        .content(reviewRequest.getContent())
        .build();

    Review saveReview = reviewRepository.save(newReview);

    return CodeReviewTreeResponseDto.from(saveReview);
  }


}
