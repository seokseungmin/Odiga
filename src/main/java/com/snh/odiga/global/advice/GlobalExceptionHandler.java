package com.snh.odiga.global.advice;

import com.snh.odiga.global.exception.InvalidOrExpiredRefreshTokenException;
import com.snh.odiga.global.exception.TokenReuseDetectedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 전역 예외 처리 클래스.
 * <p>
 * 인증/인가 관련 커스텀 예외를 처리하고 클라이언트에 적절한 응답을 반환한다.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/**
	 * [1] 이미 사용된 Refresh Token 사용 시 예외 처리.
	 *
	 * @param e TokenReuseDetectedException 예외 객체
	 * @return 401 Unauthorized 응답
	 */
	@ExceptionHandler(TokenReuseDetectedException.class)
	public ResponseEntity<String> handleTokenReuse(TokenReuseDetectedException e) {
		log.warn("[GlobalExceptionHandler] 이미 사용된 리프레시 토큰: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
	}

	/**
	 * [2] 존재하지 않거나 만료된 Refresh Token 사용 시 예외 처리.
	 *
	 * @param e InvalidOrExpiredRefreshTokenException 예외 객체
	 * @return 401 Unauthorized 응답
	 */
	@ExceptionHandler(InvalidOrExpiredRefreshTokenException.class)
	public ResponseEntity<String> handleInvalidOrExpiredRefreshToken(InvalidOrExpiredRefreshTokenException e) {
		log.warn("[GlobalExceptionHandler] 유효하지 않거나 만료된 리프레시 토큰: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
	}
}