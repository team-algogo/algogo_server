package com.ssafy.algogo.review.entity;

import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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
            columnNames = {"user_id", "submission_id"}
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
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    public void updateRequireReview(Boolean isDone) {
        this.isDone = isDone;
    }
}
