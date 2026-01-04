package com.ssafy.algogo.review.service.impl;

import com.ssafy.algogo.alarm.entity.AlarmPayload;
import com.ssafy.algogo.alarm.service.AlarmService;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.review.dto.request.CreateCodeReviewRequestDto;
import com.ssafy.algogo.review.dto.request.UpdateCodeReiewRequestDto;
import com.ssafy.algogo.review.dto.response.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.dto.response.UserCodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.UserCodeReviewResponseDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewResponseDto;
import com.ssafy.algogo.review.entity.Review;
import com.ssafy.algogo.review.entity.UserReviewReaction;
import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.program.repository.ProgramUserRepository;
import com.ssafy.algogo.review.repository.RequireReviewRepository;
import com.ssafy.algogo.review.repository.ReviewRepository;
import com.ssafy.algogo.review.repository.UserReviewReactionRepository;
import com.ssafy.algogo.review.service.ReviewService;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.submission.repository.SubmissionRepository;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final AlarmService alarmService;

    private final ReviewRepository reviewRepository;
    private final RequireReviewRepository requireReviewRepository;
    private final UserReviewReactionRepository userReviewReactionRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ProgramUserRepository programUserRepository;

    @Override
    public CodeReviewTreeResponseDto createCodeReview(
        CreateCodeReviewRequestDto createCodeReviewRequestDto, Long userId) {

        // 리뷰 작성자가 없는 유저임
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("user ID에 해당하는 데이터가 DB에 없습니다.",
                ErrorCode.USER_NOT_FOUND));

        // 리뷰 달려는 제출의 정보가 없음
        // fetch join으로 getProgramProblem, getUser, getProgram, getProblem 가져옴
        Submission targetSubmission = submissionRepository.findByIdWithAll(
                createCodeReviewRequestDto.getSubmissionId())
            .orElseThrow(() -> new CustomException("submission ID에 해당하는 데이터가 DB에 없습니다.",
                ErrorCode.SUBMISSION_NOT_FOUND));

        // 제출 작성자가 없는 유저임
        if (targetSubmission.getUser() == null) {
            throw new CustomException("제출물 작성자 정보가 없습니다.", ErrorCode.SUBMISSION_NOT_FOUND);
        }

        User targetSubmissionAuthor = targetSubmission.getUser();
        Long targetSubmissionAuthorId = targetSubmissionAuthor.getId();
        Long programId = targetSubmission.getProgramProblem().getProgram().getId();

        // 제출 작성자 active 상태 확인
        validateActiveProgramUser(targetSubmissionAuthorId, programId);

        // 대댓글 예외처리
        Review parentReview = null;
        Long parentReviewAuthorId = null;
        if (createCodeReviewRequestDto.getParentReviewId() != null) {
            // 없는 댓글에 대댓글을 달려고 함
            // fetch join으로 getUser 가져옴
            parentReview = reviewRepository.findByIdWithUser(
                    createCodeReviewRequestDto.getParentReviewId())
                .orElseThrow(() -> new CustomException("parentReview ID에 해당하는 데이터가 DB에 없습니다.",
                    ErrorCode.REVIEW_NOT_FOUND));

            parentReviewAuthorId = parentReview.getUser().getId();

            // 부모 리뷰 작성자 active 상태 확인
            validateActiveProgramUser(parentReviewAuthorId, programId);
        }

        Review newReview = Review.builder()
            .codeLine(createCodeReviewRequestDto.getCodeLine())
            .likeCount(0L)
            .parentReview(parentReview)
            .submission(targetSubmission)
            .user(user)
            .content(createCodeReviewRequestDto.getContent())
            .build();

        Review saveReview = reviewRepository.save(newReview);

        boolean isReply = saveReview.getParentReview() != null;

        // 알람을 받을 사용자 ID와 알람 타입을 저장하는 Map
        Map<Long, String> alarmRecipients = new HashMap<>();

        // 1. 댓글인 경우: Submission 작성자에게 알람
        if (!userId.equals(targetSubmissionAuthorId)) {
            alarmRecipients.put(targetSubmissionAuthorId, "REVIEW_CREATED");
        }

        // 2. 대댓글인 경우: parent review 작성자에게 알람
        if (isReply && !userId.equals(parentReviewAuthorId)) {
            alarmRecipients.put(parentReviewAuthorId, "REPLY_REVIEW");
        }

        // 알람 전송
        for (Map.Entry<Long, String> entry : alarmRecipients.entrySet()) {
            Long recipientId = entry.getKey();
            String alarmType = entry.getValue();

            alarmService.createAndSendAlarm(
                recipientId,
                alarmType,
                new AlarmPayload(
                    targetSubmission.getId(),
                    saveReview.getId(),
                    targetSubmission.getProgramProblem().getId(),
                    programId,
                    userId,
                    user.getNickname(),
                    targetSubmission.getProgramProblem().getProgram().getTitle(),
                    targetSubmission.getProgramProblem().getProblem().getTitle(),
                    null,
                    null,
                    targetSubmissionAuthor.getNickname(),
                    isReply ? saveReview.getParentReview().getId() : null,
                    isReply ? parentReviewAuthorId : null,
                    isReply ? saveReview.getParentReview().getUser().getNickname() : null
                ),
                "REPLY_REVIEW".equals(alarmType) ? "내 댓글에 대댓글이 등록되었습니다." : "내 제출물에 새로운 리뷰가 등록되었습니다."
            );
        }

        // 해당 리뷰가 루트 리뷰면..
        if (saveReview.getParentReview() == null) {
            // 내가 해야할 리뷰와 같은 제출 id에 리뷰 작성을 했다면
            // 해당 requireReview 를 가져옴
            requireReviewRepository
                .findBySubjectUserIdAndTargetSubmissionId(userId,
                    saveReview.getSubmission().getId())
                .ifPresent(requireReview -> {
                    requireReview.updateRequireReview(true);
                });

        }

        return CodeReviewTreeResponseDto.from(saveReview);
    }

    private void validateActiveProgramUser(Long userId, Long programId) {
        // 제출 작성자가 해당 그룹에 가입한 적이 없음
        ProgramUser submissionUser = programUserRepository
            .findByUserIdAndProgramId(userId, programId)
            .orElseThrow(() -> new CustomException("해당 유저는 프로그램에 포함된 유저가 아닙니다.",
                ErrorCode.SUBMISSION_NOT_FOUND));

        // 제출 작성자가 active 가 아님
        if (submissionUser.getProgramUserStatus() != ProgramUserStatus.ACTIVE) {
            throw new CustomException("해당 유저는 active 상태가 아닙니다.",
                ErrorCode.PROGRAM_USER_NOT_ACTIVE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CodeReviewListResponseDto getReviewsBySubmissionId(Long submissionId) {

        // 제출 코드 여부를 확인
        boolean exists = submissionRepository.existsById(submissionId);
        if (!exists) {
            throw new CustomException("존재하지 않는 submission ID 입니다.", ErrorCode.SUBMISSION_NOT_FOUND);
        }

        List<Review> reviews = reviewRepository.findAllBySubmission_IdOrderByCreatedAtAsc(
            submissionId);

        List<CodeReviewTreeResponseDto> reviewTree = new ArrayList<>();
        if (!reviews.isEmpty()) {
            reviewTree = buildReviewTree(reviews);
        }

        return CodeReviewListResponseDto.from(reviewTree);
    }

    @Override
    public CodeReviewTreeResponseDto editCodeReview(Long userId, Long reviewId,
        UpdateCodeReiewRequestDto updateCodeReiewRequestDto) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException("reviewID에 해당하는 리뷰가 DB에 없습니다.",
                ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new CustomException("리뷰 작성자만 수정할 수 있습니다.", ErrorCode.FORBIDDEN);
        }

        review.updateReview(updateCodeReiewRequestDto.getCodeLine(),
            updateCodeReiewRequestDto.getContent());

        return CodeReviewTreeResponseDto.from(review);
    }

    @Override
    public void deleteCodeReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException("reviewID에 해당하는 리뷰가 DB에 없습니다.",
                ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new CustomException("리뷰 작성자만 삭제할 수 있습니다.", ErrorCode.FORBIDDEN);
        }

        // 리뷰의 제출 id
        Long submissionId = review.getSubmission().getId();

//        // 부모 댓글이라면 자식 댓글부터 삭제
//        Long parentReviewId =
//            review.getParentReview() == null ? null : review.getParentReview().getId();
//        if (parentReviewId == null) {
//            // 자식 댓글 다 가져와서
//            List<Review> childReviews = reviewRepository.findAllByParentReview_Id(reviewId);
//
//            for(Review childReview : childReviews) {
//                // 자식 댓글 리엑션 삭제
//                userReviewReactionRepository.deleteByReview_Id(childReview.getId());
//                // 자식 댓글 삭제
//                reviewRepository.deleteById(childReview.getId());
//            }
//        }
//        // 본인 삭제
//        userReviewReactionRepository.deleteByReview_Id(reviewId);
        reviewRepository.delete(review);

        // user 의 required_reviews 테이블에 해당 submissionId가 있는 지
        requireReviewRepository.findBySubjectUserIdAndTargetSubmissionId(userId, submissionId)
            .ifPresent(requireReview -> {
                // 해당 submissionId로 작성된 review 가 있는지, 해당 리뷰의 parentId가 null 인지
                if (!reviewRepository.existsByUser_IdAndSubmission_IdAndParentReviewIsNull(
                    userId, submissionId)) {
                    // 없으면, required_reviews 테이블의 isDone false 로 변경
                    requireReview.updateRequireReview(false);
                }
            });
    }

    @Override
    public Boolean addCodeReviewLike(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException("reviewID에 해당하는 리뷰가 DB에 없습니다.",
                ErrorCode.REVIEW_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("userID에 해당하는 유저가 DB에 없습니다.",
                ErrorCode.USER_NOT_FOUND));

        UserReviewReaction userReviewReaction = userReviewReactionRepository.findByUser_IdAndReview_Id(
            userId, reviewId);

        if (userReviewReaction != null) {
            return false;
        }

        UserReviewReaction newReaction = UserReviewReaction.builder()
            .user(user)
            .review(review)
            .build();

        UserReviewReaction SaveUserReviewReaction = userReviewReactionRepository.save(newReaction);

        review.addReviewLikeCount();

        return true;
    }

    @Override
    public Boolean deleteCodeReviewLike(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException("reviewID에 해당하는 리뷰가 DB에 없습니다.",
                ErrorCode.REVIEW_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("userID에 해당하는 유저가 DB에 없습니다.",
                ErrorCode.USER_NOT_FOUND));

        UserReviewReaction userReviewReaction = userReviewReactionRepository.findByUser_IdAndReview_Id(
            userId, reviewId);

        if (userReviewReaction == null) {
            return false;
        }

        userReviewReactionRepository.deleteByReview_Id(reviewId);

        review.deleteReviewLikeCount();

        return true;
    }


    private List<CodeReviewTreeResponseDto> buildReviewTree(List<Review> reviews) {

        Map<Long, CodeReviewTreeResponseDto> dtoMap = new LinkedHashMap<>();
        List<CodeReviewTreeResponseDto> roots = new ArrayList<>();

        // 1) 엔티티 -> DTO 변환 + 루트 댓글 수집
        for (Review review : reviews) {

            // 모든 review를 dto화 시켜서 id로 매핑
            CodeReviewTreeResponseDto reviewDto = CodeReviewTreeResponseDto.from(review);
            dtoMap.put(reviewDto.reviewId(), reviewDto);

            // 부모가 없음 -> 최상위 댓글
            if (reviewDto.parentReviewId() == null) {
                roots.add(reviewDto);
            }
        }

        // 2) 부모-자식 연결 (대댓글)
        for (Review review : reviews) {
            if (review.getParentReview() != null) {
                Long parentId = review.getParentReview().getId();

                CodeReviewTreeResponseDto parentDto = dtoMap.get(parentId);
                CodeReviewTreeResponseDto childDto = dtoMap.get(review.getId());

                if (parentDto != null && childDto != null) {
                    parentDto.children().add(childDto);
                }
            }
        }

        return roots;
    }

    @Override
    @Transactional(readOnly = true)
    public RequiredCodeReviewListResponseDto getRequiredReviews(Long userId) {
        List<RequiredCodeReviewResponseDto> requiredCodeReviewResponseDtos = requireReviewRepository.getRequiredReviews(
            userId);

        return RequiredCodeReviewListResponseDto.from(requiredCodeReviewResponseDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public UserCodeReviewListResponseDto getReceiveReviews(Long userId, Integer page,
        Integer size) {

        // 디폴트 값 설정
        int pageSafe = (page == null || page < 0) ? 0 : page;
        int sizeSafe = (size == null || size <= 0) ? 10 : Math.min(size, 100);

        Page<UserCodeReviewResponseDto> userCodeReviewResponseDtos = reviewRepository.getReceiveReviews(
            userId, pageSafe, sizeSafe);

        PageInfo pageInfo = PageInfo.of(userCodeReviewResponseDtos);

        return UserCodeReviewListResponseDto.from(
            pageInfo,
            userCodeReviewResponseDtos.getContent()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserCodeReviewListResponseDto getDoneReviews(Long userId, Integer page, Integer size) {

        // 디폴트 값 설정
        int pageSafe = (page == null || page < 0) ? 0 : page;
        int sizeSafe = (size == null || size <= 0) ? 10 : Math.min(size, 100);

        Page<UserCodeReviewResponseDto> userCodeReviewResponseDtos = reviewRepository.getDoneReviews(
            userId, pageSafe, sizeSafe);

        PageInfo pageInfo = PageInfo.of(userCodeReviewResponseDtos);

        return UserCodeReviewListResponseDto.from(
            pageInfo,
            userCodeReviewResponseDtos.getContent()
        );
    }


}
