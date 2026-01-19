package com.ssafy.algogo.user.entity;

import com.ssafy.algogo.common.utils.BaseTime;
import com.ssafy.algogo.user.service.impl.UserServiceImpl;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = { "email" }),
        @UniqueConstraint(name = "uk_user_nickname", columnNames = { "nickname" })
})
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    @Builder.Default
    private String description = "안녕하세요";

    @Column(name = "profile_image")
    @Builder.Default
    private String profileImage = UserServiceImpl.DEFAULT_USER_IMAGE;

    @NotNull
    private String nickname;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole userRole;

    public void updateUserInfo(String nickname, String description) {
        this.nickname = nickname;
        this.description = description;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

}
