package com.br.azevedo.infra.log.method;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class MethodLoggingAspect {

    @Pointcut("@annotation(logging)")
    public void callAt(MethodLoggable logging) {}

    @Around("callAt(logging)")
    public Object around(ProceedingJoinPoint pjp, MethodLoggable logging) throws Throwable {
        long start = System.currentTimeMillis();
        var methodName = StringUtils.defaultIfEmpty(logging.value(), pjp.getSignature().getName());
        try {
            log.info("INICIO - [Executing method: {}()]", methodName);
            return pjp.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("FIM - [Executing method: {}()] - Duration: {} ms", methodName, duration);
        }
    }
}
