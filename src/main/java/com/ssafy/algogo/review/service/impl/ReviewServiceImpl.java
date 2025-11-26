package com.ssafy.algogo.review.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.review.dto.request.CreateCodeReviewRequestDto;
import com.ssafy.algogo.review.dto.request.UpdateCodeReiewRequestDto;
import com.ssafy.algogo.review.dto.response.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewResponseDto;
import com.ssafy.algogo.review.entity.Review;
import com.ssafy.algogo.review.repository.RequireReviewRepository;
import com.ssafy.algogo.review.repository.ReviewRepository;
import com.ssafy.algogo.review.service.ReviewService;
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
  private final RequireReviewRepository requireReviewRepository;
  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;

  @Override
  public CodeReviewTreeResponseDto createCodeReview(CreateCodeReviewRequestDto createCodeReviewRequestDto, Long userId) {

    Submission submission = submissionRepository.findById(createCodeReviewRequestDto.getSubmissionId())
        .orElseThrow(() -> new CustomException("submission ID에 해당하는 데이터가 DB에 없습니다.",
            ErrorCode.SUBMISSION_NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException("user ID에 해당하는 데이터가 DB에 없습니다.", ErrorCode.USER_NOT_FOUND));

    Review parentReview = null;
    if(createCodeReviewRequestDto.getParentReviewId() != null) {
      parentReview = reviewRepository.findById(createCodeReviewRequestDto.getParentReviewId())
          .orElseThrow(() -> new CustomException("parentReview ID에 해당하는 데이터가 DB에 없습니다.",
              ErrorCode.REVIEW_NOT_FOUND));
    }

    Review newReview = Review.builder()
        .codeLine(createCodeReviewRequestDto.getCodeLine())
        .likeCount(0L)
        .parentReview(parentReview)
        .submission(submission)
        .user(user)
        .content(createCodeReviewRequestDto.getContent())
        .build();

    Review saveReview = reviewRepository.save(newReview);

    return CodeReviewTreeResponseDto.from(saveReview);
  }

  @Override
  public CodeReviewListResponseDto getReviewsBySubmissionId(Long submissionId) {

    // 제출 코드 여부를 확인
    boolean exists = submissionRepository.existsById(submissionId);
    if(!exists) {
      throw new CustomException("존재하지 않는 submission ID 입니다.", ErrorCode.SUBMISSION_NOT_FOUND);
    }

    List<Review> reviews = reviewRepository.findAllBySubmission_IdOrderByCreatedAtAsc(submissionId);

    List<CodeReviewTreeResponseDto> reviewTree = new ArrayList<>();
    if(!reviews.isEmpty()) {
     reviewTree = buildReviewTree(reviews);
    }

    return CodeReviewListResponseDto.from(reviewTree);
  }

  @Override
  public CodeReviewTreeResponseDto editCodeReview(Long userId, Long reviewId, UpdateCodeReiewRequestDto updateCodeReiewRequestDto) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException("reviewID에 해당하는 리뷰가 DB에 없습니다.", ErrorCode.REVIEW_NOT_FOUND));

    if(!review.getUser().getId().equals(userId)) {
      throw new CustomException("리뷰 작성자만 수정할 수 있습니다.", ErrorCode.FORBIDDEN);
    }

    review.updateReview(updateCodeReiewRequestDto.getCodeLine(), updateCodeReiewRequestDto.getContent());

    return CodeReviewTreeResponseDto.from(review);
  }

  private List<CodeReviewTreeResponseDto> buildReviewTree(List<Review> reviews) {

    Map<Long, CodeReviewTreeResponseDto> dtoMap = new LinkedHashMap<>();
    List<CodeReviewTreeResponseDto> roots = new ArrayList<>();

    // 1) 엔티티 -> DTO 변환 + 루트 댓글 수집
    for(Review review : reviews) {

      // 모든 review를 dto화 시켜서 id로 매핑
      CodeReviewTreeResponseDto reviewDto = CodeReviewTreeResponseDto.from(review);
      dtoMap.put(reviewDto.reviewId(), reviewDto);

      // 부모가 없음 -> 최상위 댓글
      if (reviewDto.parentReviewId() == null) {
        roots.add(reviewDto);
      }
    }

    // 2) 부모-자식 연결 (대댓글)
    for(Review review : reviews) {
      if(review.getParentReview() != null) {
        Long parentId = review.getParentReview().getId();

        CodeReviewTreeResponseDto parentDto = dtoMap.get(parentId);
        CodeReviewTreeResponseDto childDto = dtoMap.get(review.getId());

        if(parentDto != null && childDto != null) {
          parentDto.children().add(childDto);
        }
      }
    }

    return roots;
  }

  public RequiredCodeReviewListResponseDto getRequiredReviews(Long userId) {
    List<RequiredCodeReviewResponseDto> requiredCodeReviewResponseDtos = requireReviewRepository.getRequiredReviews(userId);

    return RequiredCodeReviewListResponseDto.from(requiredCodeReviewResponseDtos);
  }
}
