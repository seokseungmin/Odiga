package com.snh.odiga.common;

/**
 * 보안 관련 상수를 정의한 클래스.
 * <p>
 * 이 클래스는 인스턴스화할 수 없으며, 토큰 및 쿠키 이름 등의 상수를 제공한다.
 */
public final class SecurityConstants {

	private SecurityConstants() {
		// 인스턴스화 방지
	}

	/** 로그아웃 요청을 처리할 엔드포인트 URI */
	public static final String LOGOUT_URI = "/logout";

	/** 로그아웃 요청에 사용할 HTTP 메서드 */
	public static final String LOGOUT_METHOD = "POST";

	/**
	 * 토큰의 종류를 정의하는 상수 클래스 (Access / Refresh 구분용).
	 * <p>
	 * [SecurityConstants.TokenCategory]
	 */
	public static final class TokenCategory {

		/** 액세스 토큰 구분자 */
		public static final String ACCESS = "access";

		/** 리프레시 토큰 구분자 */
		public static final String REFRESH = "refresh";

		private TokenCategory() {
			// 인스턴스화 방지
		}
	}

	/**
	 * 클라이언트에 저장되는 쿠키 이름을 정의하는 상수 클래스.
	 * <p>
	 * [SecurityConstants.CookieName]
	 */
	public static final class CookieName {

		/** 액세스 토큰을 담는 쿠키 이름 */
		public static final String ACCESS = "Authorization";

		/** 리프레시 토큰을 담는 쿠키 이름 */
		public static final String REFRESH = "Refresh-Token";

		private CookieName() {
			// 인스턴스화 방지
		}
	}
}