package com.ssafy.algogo.alarm.entity;

import com.ssafy.algogo.common.utils.BaseTime;
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
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    name = "alarms"
)
public class Alarm extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "alarm_type_id")
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
