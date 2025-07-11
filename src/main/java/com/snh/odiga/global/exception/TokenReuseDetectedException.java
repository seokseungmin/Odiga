package com.snh.odiga.global.exception;

/**
 * 이미 사용된 Refresh Token이 감지되었을 때 발생하는 예외 클래스.
 * <p>
 * 보안 강화를 위해 재사용된 토큰을 차단하고,
 * 사용자에게 로그아웃이나 재인증을 요구할 수 있도록 예외를 발생시킨다.
 */
public class TokenReuseDetectedException extends RuntimeException {

	/**
	 * 기본 생성자.
	 * <p>
	 * Refresh Token이 재사용되었을 때 던져지는 예외 메시지를 설정한다.
	 */
	public TokenReuseDetectedException() {
		super("이미 사용된 Refresh Token 입니다.");
	}
}