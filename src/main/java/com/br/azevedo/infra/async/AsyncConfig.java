package com.br.azevedo.infra.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@EnableAsync
@ConditionalOnProperty(value = "async.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncConfig implements AsyncConfigurer {

    private final AsyncProperties asyncProperties;

    public AsyncConfig(AsyncProperties asyncProperties) {
        this.asyncProperties = asyncProperties;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(asyncProperties.getCorePoolSize());
        executor.setQueueCapacity(asyncProperties.getQueueCapacity());
        executor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        executor.setThreadNamePrefix(asyncProperties.getThreadNamePrefix());
        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        String logMessage = "Erro ao Processar chamada assincrona  - %s - %s";
        return (exception, method, methodParam) ->
                log.error(String.format(logMessage, method.getName(), exception.getMessage()), methodParam, exception);
    }
}
