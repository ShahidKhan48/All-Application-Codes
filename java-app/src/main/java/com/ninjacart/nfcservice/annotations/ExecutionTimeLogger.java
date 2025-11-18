package com.ninjacart.nfcservice.annotations;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class ExecutionTimeLogger {

    @Around("@annotation(com.ninjacart.nfcservice.annotations.LogExecutionTime)")
    public Object trackTime(ProceedingJoinPoint pjp) throws Throwable {
        StopWatch watch = new StopWatch();
        watch.start();
        Object obj = pjp.proceed();
        watch.stop();

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = shortenClassName(signature.getDeclaringTypeName());
        LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);

        TimeUnit timeUnit = annotation.timeUnit();
        long time = watch.getTime(timeUnit);

        String annotationMessage = annotation.message();
        logMessage(methodName, className, timeUnit, time, annotationMessage);
        return obj;
    }

    private static void logMessage(String methodName, String classNameShort, TimeUnit timeUnit, long time, String annotationMessage) {
        String slowExecutionString;
        if (timeUnit == TimeUnit.MILLISECONDS && time > 3500) {
            slowExecutionString = "GT2500";
        } else if (timeUnit == TimeUnit.MILLISECONDS && time > 2500) {
            slowExecutionString = "GT2500";
        } else if (timeUnit == TimeUnit.MILLISECONDS && time > 2000) {
            slowExecutionString = "GT2000";
        } else if (timeUnit == TimeUnit.MILLISECONDS && time > 1500) {
            slowExecutionString = "GT1500";
        } else if (timeUnit == TimeUnit.MILLISECONDS && time > 1000) {
            slowExecutionString = "GT1000";
        } else if (timeUnit == TimeUnit.MILLISECONDS && time > 500) {
            slowExecutionString = "GT500";
        } else {
            slowExecutionString = "";
        }

        log.info("ExecTime {} : {}.{}, time taken : {}{}, {}", slowExecutionString, classNameShort, methodName, time, timeUnit.name(), annotationMessage);
    }

    private String shortenClassName(String classNameFull) {
        if (classNameFull == null || classNameFull.isEmpty()) {
            return "NA";
        }
        try {
            String[] split = classNameFull.split("\\.");
            return split[split.length - 1];
        } catch (Exception exception) {
            return "NA";
        }
    }
}
