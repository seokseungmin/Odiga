package com.snh.odiga.service;

import com.snh.odiga.global.exception.InvalidOrExpiredRefreshTokenException;
import com.snh.odiga.util.CookieUtil;
import com.snh.odiga.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * 사용자 로그아웃 처리를 담당하는 서비스 클래스.
 * <p>
 * Refresh Token 유효성 검사, DB 토큰 삭제, 쿠키 삭제, 로그 기록 및 SecurityContext 초기화를 수행한다.
 */
@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LogoutService {

	private final RefreshTokenService refreshTokenService;
	private final JWTUtil jwtUtil;

	/**
	 * 로그아웃 요청을 처리한다.
	 * <p>
	 * [LogoutService] -
	 * 1. DB에서 Refresh Token의 존재 여부를 검사한다.
	 * 2. DB에서 해당 Refresh Token을 삭제한다.
	 * 3. 클라이언트에 설정된 Access Token, Refresh Token 쿠키를 삭제 요청한다.
	 * 4. 로그아웃 완료 메시지를 클라이언트에 전송한다.
	 * 5. 로그 기록 후 SecurityContext를 초기화한다.
	 *
	 * @param refreshToken 로그아웃에 사용할 Refresh Token
	 * @param request      HttpServletRequest
	 * @param response     HttpServletResponse
	 * @throws IOException 클라이언트 응답 작성 중 발생할 수 있는 예외
	 */
	@Transactional
	public void logout(String refreshToken, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// [1] DB에서 Refresh Token의 존재 여부를 검사
		refreshTokenService.findByRefreshToken(refreshToken)
				.orElseThrow(InvalidOrExpiredRefreshTokenException::new);

		// [2] DB에서 Refresh Token 삭제
		refreshTokenService.deleteByRefreshToken(refreshToken);
		log.info("[LogoutService] 기존 Refresh Token 삭제 완료 - token={}", refreshToken);

		// [3] 쿠키 삭제 (Access Token, Refresh Token)
		ResponseCookie clearAccess = CookieUtil.createResponseCookie(
				SecurityConstants.CookieName.ACCESS,
				null,
				0);
		ResponseCookie clearRefresh = CookieUtil.createResponseCookie(
				SecurityConstants.CookieName.REFRESH,
				null,
				0);
		response.addHeader("Set-Cookie", clearAccess.toString());
		response.addHeader("Set-Cookie", clearRefresh.toString());
		log.info("[LogoutService] 쿠키 삭제 완료 - Access, Refresh");

		// [4] 로그아웃 완료 메시지 응답 전송
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write("로그아웃 처리되었습니다. 다시 로그인해 주세요.");

		// [5] 로그 기록 - 토큰으로부터 사용자 정보 추출하여 로깅
		String oauthId = jwtUtil.getOauthId(refreshToken);
		String username = jwtUtil.getUsername(refreshToken);
		log.info("[LogoutService] 로그아웃 완료 - 사용자명: {}, OAuth ID: {}, IP: {}",
				username, oauthId, request.getRemoteAddr());

		// [6] SecurityContext 초기화 (세션 정보 제거)
		SecurityContextHolder.clearContext();
		log.info("[LogoutService] SecurityContext 초기화 완료");
	}
}