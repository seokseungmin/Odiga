package com.snh.odiga.jwt;

import com.snh.odiga.service.LogoutService;
import com.snh.odiga.util.CookieUtil;
import com.snh.odiga.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * JWT 기반 인증 시스템에서 로그아웃 요청을 처리하는 커스텀 필터 클래스.
 * <p>
 * 요청 URI와 메서드를 확인하여 로그아웃 요청인 경우,
 * 쿠키에서 Refresh Token을 추출하고, 토큰의 유효성 및 카테고리를 검증한 후
 * LogoutService를 호출하여 로그아웃 처리를 수행한다.
 * 이 필터는 Stateless 환경에서 Refresh Token 무효화와 세션 정리를 지원한다.
 */
@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

	private final JWTUtil jwtUtil;
	private final LogoutService logoutService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		handleLogout((HttpServletRequest) request, (HttpServletResponse) response, chain);
	}

	/**
	 * [1] 로그아웃 요청을 처리한다.
	 * URI와 메서드를 확인하여 로그아웃 요청이 맞다면, 리프레시 토큰을 검증 후 로그아웃 로직을 수행한다.
	 *
	 * @param request  HttpServletRequest 객체
	 * @param response HttpServletResponse 객체
	 * @param chain    FilterChain 객체
	 * @throws IOException      입출력 예외 발생 시
	 * @throws ServletException 서블릿 예외 발생 시
	 */
	private void handleLogout(HttpServletRequest request, HttpServletResponse response,
							  FilterChain chain) throws IOException, ServletException {
		// [1-1] 로그아웃 요청이 아닌 경우 다음 필터로 전달한다.
		if (!isLogoutRequest(request)) {
			chain.doFilter(request, response);
			return;
		}

		log.info("[CustomLogoutFilter] 로그아웃 요청 감지 - URI: {}, Method: {}",
				request.getRequestURI(), request.getMethod());

		// [2] 쿠키에서 리프레시 토큰 추출
		String refreshToken = CookieUtil.getCookieValue(request, SecurityConstants.CookieName.REFRESH);
		if (refreshToken == null) {
			log.warn("[CustomLogoutFilter] 로그아웃 실패 - 쿠키에 리프레시 토큰이 없습니다.");
		} else {
			log.info("[CustomLogoutFilter] 리프레시 토큰 추출 성공 - {}", refreshToken);
		}

		// [3] 토큰 유효성 및 카테고리 검증
		if (!validateRequest(refreshToken, response)) {
			log.warn("[CustomLogoutFilter] 로그아웃 실패 - 유효하지 않은 리프레시 토큰입니다.");
			return;
		}

		// [4] LogoutService를 호출하여 로그아웃 처리 수행
		log.info("[CustomLogoutFilter] 로그아웃 처리 시작");
		logoutService.logout(refreshToken, request, response);
		log.info("[CustomLogoutFilter] 로그아웃 처리 완료");
	}

	/**
	 * [1-1] 현재 요청이 로그아웃 요청인지 확인한다.
	 *
	 * @param request HttpServletRequest 객체
	 * @return 로그아웃 요청이면 {@code true}, 아니면 {@code false}
	 */
	private boolean isLogoutRequest(HttpServletRequest request) {
		boolean result = SecurityConstants.LOGOUT_URI.equals(request.getRequestURI())
				&& SecurityConstants.LOGOUT_METHOD.equalsIgnoreCase(request.getMethod());
		log.debug("[CustomLogoutFilter] 로그아웃 요청 여부: {}", result);
		return result;
	}

	/**
	 * [3-1] 토큰이 없거나 만료되었는지 확인한다.
	 *
	 * @param token Refresh Token 문자열
	 * @return 토큰이 없거나 만료되었으면 {@code true}, 그렇지 않으면 {@code false}
	 */
	private boolean isInvalidRefreshToken(String token) {
		boolean result = (token == null || jwtUtil.isExpired(token));
		log.debug("[CustomLogoutFilter] 리프레시 토큰 유효성 검사 결과 (유효하지 않음): {}", result);
		return result;
	}

	/**
	 * [3-2] 토큰이 'refresh' 카테고리인지 확인한다.
	 *
	 * @param token Refresh Token 문자열
	 * @return 'refresh' 카테고리이면 {@code true}, 그렇지 않으면 {@code false}
	 */
	private boolean isRefreshCategory(String token) {
		boolean result = SecurityConstants.TokenCategory.REFRESH.equals(jwtUtil.getCategory(token));
		log.debug("[CustomLogoutFilter] 리프레시 토큰 카테고리 여부: {}", result);
		return result;
	}

	/**
	 * [3] 토큰 유효성 및 카테고리를 검증한다.
	 *
	 * @param refreshToken Refresh Token 문자열
	 * @param response     HttpServletResponse 객체
	 * @return 검증 성공 시 {@code true}, 실패 시 {@code false}
	 */
	private boolean validateRequest(String refreshToken, HttpServletResponse response) {
		if (isInvalidRefreshToken(refreshToken)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return false;
		}
		if (!isRefreshCategory(refreshToken)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return false;
		}
		return true;
	}
}