package com.snh.odiga.trace.aop;

import com.snh.odiga.trace.LogTrace;
import com.snh.odiga.trace.TraceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * [TraceAspect]
 * <p>
 * Controller, Service, Repository 계층의 모든 메서드 호출을 AOP를 통해 추적하는 Aspect 클래스.
 * LogTrace (ThreadLocalLogTrace)를 사용하여 메서드 호출 전후에 시작, 종료, 예외를 기록한다.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TraceAspect {

	private final LogTrace logTrace;

	// 포인트컷: controller, service, repository 패키지의 모든 public 메서드
	@Around("execution(public * com.snh.odiga.controller..*(..)) " +
			"|| execution(public * com.snh.odiga.service..*(..)) " +
			"|| execution(public * com.snh.odiga.repository..*(..))")
	public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
		// 메서드 시그니처로 메시지 구성
		String message = joinPoint.getSignature().toShortString();
		TraceStatus status = logTrace.begin(message);

		try {
			Object result = joinPoint.proceed();
			logTrace.end(status);
			return result;
		} catch(Exception e) {
			logTrace.exception(status, e);
			throw e;
		}
	}
}