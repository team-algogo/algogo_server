package com.ssafy.algogo.alarm.entity;

import com.ssafy.algogo.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "alarm_type")
    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    // 이런식으로 AlarmType DTO를 만들고 직접 참조하게 해서, 데이터를 꺼내올때 JSON 형식으로 자동 변환, 매핑 해주는 형식
    // alarm_type.getPayload.getInvited_id 가능
    // @NotNull -> 이거  ㅇ왜 낫ㅇ널임?
    @JdbcTypeCode(SqlTypes.JSON) // JSON 타입 제대로 생기는지 DB 체크
    private AlarmPayload payload;

    @NotNull
    private String message;

    @NotNull
    @Column(name = "is_read")
    private Boolean isRead;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
