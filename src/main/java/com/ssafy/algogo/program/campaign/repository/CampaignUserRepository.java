package com.ssafy.algogo.program.campaign.repository;

import com.ssafy.algogo.program.campaign.entity.CampaignUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CampaignUserRepository extends JpaRepository<CampaignUser, Long> {

}
