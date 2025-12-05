package com.ssafy.algogo.review.entity;

import com.ssafy.algogo.common.utils.BaseTime;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    name = "reviews"
)
public class Review extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String content;

    @Column(name = "code_line")
    private Long codeLine;

    @NotNull
    @Column(name = "like_count")
    private Long likeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parent_review_id")
    private Review parentReview;

    public void updateReview(Long codeLine, String content) {
        this.codeLine = codeLine;
        this.content = content;
        this.modifiedAt = LocalDateTime.now();
    }

    public void addReviewLikeCount() {
        this.likeCount++;
    }

    public void deleteReviewLikeCount() {
        this.likeCount--;
    }

}
