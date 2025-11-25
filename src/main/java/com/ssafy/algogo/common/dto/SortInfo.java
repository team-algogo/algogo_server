package com.ssafy.algogo.common.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public record SortInfo (
    String by,
    String direction
){
    public static SortInfo of(Page<?> page){
        Sort.Order order = page.getSort().stream().findFirst()
                .orElse(Sort.Order.desc("createdAt"));
        return new SortInfo(
                order.getProperty(),
                order.getDirection().name()
        );
    }
}
