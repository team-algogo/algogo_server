package com.ssafy.algogo.common.utils;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseTime {


    // -> LocalDateTime vs Timestamp
    @CreatedDate
    private LocalDateTime created_at;

    @LastModifiedDate
    private LocalDateTime modified_at;
}
