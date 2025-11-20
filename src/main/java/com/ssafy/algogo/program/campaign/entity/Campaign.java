package com.ssafy.algogo.program.campaign.entity;

import com.ssafy.algogo.program.entity.Program;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Campaign extends Program {

    @NotNull
    private Long capacity;

    @NotNull
    @Column(name = "start_date")
    private LocalDateTime startDate; // 이거 Timestamp로 해야하나? LocalDateTime은 불변시간이라 음 음 되나?

    @NotNull
    @Column(name = "end_date")
    private LocalDateTime endDate; // 이거 Timestamp로 해야하나? LocalDateTime은 불변시간이라 음 음 되나?


}
