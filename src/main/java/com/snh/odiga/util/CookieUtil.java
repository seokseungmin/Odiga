package com.snh.odiga.util;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

/**
 * 쿠키 관련 유틸리티 클래스.
 * <p>
 * ResponseCookie 생성, 쿠키 값 추출 및 쿠키 삭제 기능을 제공한다.
 */
public class CookieUtil {

	/**
	 * 응답용 쿠키(ResponseCookie)를 생성한다.
	 *
	 * <p>
	 * 크로스 도메인 전송을 위해 SameSite=None + Secure=true 설정이 필요할 수 있음.
	 * 로컬 개발 환경에서는 SameSite=Strict, secure=false를 사용하며, 운영 환경에서는 적절히 수정한다.
	 *
	 * @param key    쿠키 이름
	 * @param value  쿠키 값
	 * @param maxAge 쿠키 유효 시간 (초 단위)
	 * @return 생성된 ResponseCookie 객체
	 */
	public static ResponseCookie createResponseCookie(String key, String value, int maxAge) {
		return ResponseCookie.from(key, value)
				.httpOnly(true)      // 자바스크립트에서 접근 불가
				.path("/")           // 모든 경로에 대해 쿠키 사용
				.maxAge(maxAge)      // 쿠키 만료 시간 설정
				.domain("localhost") // 배포 시 도메인에 맞게 수정 필요
				.sameSite("Strict")  // SameSite 옵션 (Strict, Lax, None 중 선택)
				.secure(false)       // HTTPS 환경에서만 전송할지 여부
				.build();
	}

	/**
	 * 요청 객체에서 특정 이름의 쿠키 값을 추출한다.
	 *
	 * <p>
	 * [CookieUtil] - 쿠키가 존재하지 않을 경우 null을 반환한다.
	 *
	 * @param request 요청 객체
	 * @param key     쿠키 이름
	 * @return 쿠키 값 (없으면 null)
	 */
	public static String getCookieValue(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (key.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * 응답에 설정된 쿠키를 삭제(클리어)한다.
	 * <p>
	 * [CookieUtil] - 각 쿠키에 대해 최대 생명 주기를 0으로 설정하여 브라우저가 쿠키를 삭제하도록 요청한다.
	 *
	 * @param response    응답 객체
	 * @param cookieNames 삭제할 쿠키 이름들
	 */
	public static void clearCookies(HttpServletResponse response, String... cookieNames) {
		for (String key : cookieNames) {
			ResponseCookie clearedCookie = ResponseCookie.from(key, "")
					.httpOnly(true)
					.path("/")
					.maxAge(0)           // maxAge를 0으로 설정하여 삭제 요청
					.domain("localhost") // 필요에 따라 도메인 수정
					.sameSite("Strict")
					.secure(false)
					.build();
			response.addHeader("Set-Cookie", clearedCookie.toString());
		}
	}
}