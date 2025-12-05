package com.ssafy.algogo.review.entity;

import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    name = "required_reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_required_review_user_submission",
            columnNames = {"user_id", "target_submission_id"}
        )
    }
)
public class RequireReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "is_done",
        columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0"
    )
    private Boolean isDone;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    /*
     * 리뷰를 해야하는 쪽(리뷰어)의 제출
     * */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "subject_submission_id")
    private Submission subjectSubmission;

    /*
     * 리뷰를 받는 쪽(제출자)의 제출
     * */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "target_submission_id")
    private Submission targetSubmission;

    public void updateRequireReview(Boolean isDone) {
        this.isDone = isDone;
    }
}

