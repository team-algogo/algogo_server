package com.ssafy.algogo.common.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "aiExecutor")
    public Executor aiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // 동시에 5개 AI 평가
        executor.setMaxPoolSize(10);      // 최대 10개까지 허용
        executor.setQueueCapacity(200);   // 대기열
        executor.setThreadNamePrefix("AI-EVAL-");
        executor.initialize();
        return executor;
    }

}
