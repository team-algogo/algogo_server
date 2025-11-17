package com.ssafy.algogo.program.campaign.entity;

import com.ssafy.algogo.program.entity.ProgramUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class CampaignUser extends ProgramUser {

    @NotNull
    @Column(name = "penalty_cnt")
    private Long penaltyCnt;

}
