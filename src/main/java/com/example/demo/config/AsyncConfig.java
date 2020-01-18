package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig extends AsyncConfigurerSupport {
    @Value("${async.core.poolsize}")
    int corePoolSize;
    @Value("${async.max.poolsize}")
    int maxPoolSize;
    @Value("${async.queue.capacity}")
    int queueCapacity;
    @Value("${async.thread.name.prefix}")
    String threadNamePrefix;

    // Thread pool 설정 (디폴트는 Thread가 매번 생성됨)
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
