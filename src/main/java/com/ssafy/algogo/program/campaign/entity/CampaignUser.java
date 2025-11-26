package com.ssafy.algogo.program.campaign.entity;

import com.ssafy.algogo.program.entity.ProgramUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "campaigns_users"
)
public class CampaignUser extends ProgramUser {

    @NotNull
    @Column(name = "penalty_cnt")
    private Long penaltyCnt;

}
