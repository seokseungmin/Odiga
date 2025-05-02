package com.snh.odiga.global.exception;

/**
 * 유효하지 않거나 만료된 Refresh Token에 대한 예외 클래스.
 * <p>
 * Refresh Token이 DB에 존재하지 않거나,
 * 이미 무효화된(삭제되었거나 재사용된) 토큰을 사용하려 할 때 발생하며,
 * 클라이언트가 비정상적인 방식으로 토큰을 재요청하는 경우에 예외를 처리한다.
 */
public class InvalidOrExpiredRefreshTokenException extends RuntimeException {

	/**
	 * 기본 생성자.
	 * <p>
	 * DB에 존재하지 않거나 이미 무효화된 Refresh Token에 대해
	 * 예외 메시지를 설정한다.
	 */
	public InvalidOrExpiredRefreshTokenException() {
		super("DB에 존재하지 않거나 이미 무효화된 Refresh 토큰입니다.");
	}
}
