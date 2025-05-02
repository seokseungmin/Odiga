package com.snh.odiga.jwt;

import com.snh.odiga.dto.CustomOAuth2User;
import com.snh.odiga.dto.OAuth2UserDto;
import com.snh.odiga.dto.TokenPair;
import com.snh.odiga.global.exception.InvalidOrExpiredRefreshTokenException;
import com.snh.odiga.service.RefreshTokenService;
import com.snh.odiga.util.CookieUtil;
import com.snh.odiga.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 필터 클래스.
 * <p>
 * 모든 요청마다 실행되는 OncePerRequestFilter를 상속하여,
 * Access Token이 유효하면 인증 객체를 설정하고, Access Token이 만료된 경우에는 Refresh Token으로
 * 토큰을 재발급 받아 인증 처리를 수행한다.
 */
@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

	private final JWTUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		String accessToken = CookieUtil.getCookieValue(request, SecurityConstants.CookieName.ACCESS);
		String refreshToken = CookieUtil.getCookieValue(request, SecurityConstants.CookieName.REFRESH);

		log.info("[JWTFilter] 받은 Access Token: {}", accessToken);
		log.info("[JWTFilter] 받은 Refresh Token: {}", refreshToken);

		// Case 1: Access Token이 존재하고 유효한 경우
		if (accessToken != null && !jwtUtil.isExpired(accessToken)) {
			if (!SecurityConstants.TokenCategory.ACCESS.equals(jwtUtil.getCategory(accessToken))) {
				log.warn("[JWTFilter] Access 토큰 카테고리 오류: {}", jwtUtil.getCategory(accessToken));
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 Access 토큰입니다.");
				return;
			}
			authenticateFromToken(accessToken, request, response);
			filterChain.doFilter(request, response);
			return;
		}

		// Case 2: Access Token이 null이거나 만료된 경우
		if ((accessToken == null || jwtUtil.isExpired(accessToken))
				&& refreshToken != null && !jwtUtil.isExpired(refreshToken)) {
			if (!SecurityConstants.TokenCategory.REFRESH.equals(jwtUtil.getCategory(refreshToken))) {
				log.warn("[JWTFilter] Refresh 토큰 카테고리 오류: {}", jwtUtil.getCategory(refreshToken));
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 Refresh 토큰입니다.");
				return;
			}

			String username = jwtUtil.getUsername(refreshToken);
			String oauthId = jwtUtil.getOauthId(refreshToken);
			String role = jwtUtil.getRole(refreshToken);

			refreshTokenService.findByRefreshToken(refreshToken)
					.orElseThrow(InvalidOrExpiredRefreshTokenException::new);

			TokenPair newTokens = refreshTokenService.rotateTokenAndGenerate(
					refreshToken, username, oauthId, role, request.getRemoteAddr());

			ResponseCookie accessCookie = CookieUtil.createResponseCookie(
					SecurityConstants.CookieName.ACCESS, newTokens.accessToken(), 600);
			ResponseCookie refreshCookie = CookieUtil.createResponseCookie(
					SecurityConstants.CookieName.REFRESH, newTokens.refreshToken(), 86400);
			response.addHeader("Set-Cookie", accessCookie.toString());
			response.addHeader("Set-Cookie", refreshCookie.toString());

			log.info("[JWTFilter] 재발급된 AccessToken = {}", newTokens.accessToken());
			log.info("[JWTFilter] 재발급된 RefreshToken = {}", newTokens.refreshToken());

			authenticateFromToken(newTokens.accessToken(), request, response);
			filterChain.doFilter(request, response);
			return;
		}

		// Case 3: 모든 조건에 해당하지 않으면 401 상태 코드 반환
		log.info("[JWTFilter] 유효한 토큰이 존재하지 않습니다.");
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증되지 않은 요청입니다.");
	}

	/**
	 * JWT 토큰으로부터 사용자 정보를 추출하여 SecurityContextHolder에 인증 객체를 등록한다.
	 * <p>
	 * [JWTFilter] - 토큰에 저장된 IP와 현재 요청의 IP를 비교하여 일치하지 않을 경우 인증을 거부하고,
	 * 쿠키를 삭제 후 재로그인을 요구하는 에러를 반환한다.
	 *
	 * @param token    JWT 토큰
	 * @param request  HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException 요청 처리 중 에러 발생 시
	 */
	private void authenticateFromToken(String token, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// 토큰에 저장된 IP 추출
		String tokenIp = jwtUtil.getIp(token);
		// 현재 요청의 IP 추출
		String requestIp = request.getRemoteAddr();

		// IP가 일치하지 않으면 인증 거부
		if (!requestIp.equals(tokenIp)) {
			log.warn("[JWTFilter] IP 불일치: 토큰에 저장된 IP = {}, 요청 IP = {}", tokenIp, requestIp);
			SecurityContextHolder.clearContext();
			CookieUtil.clearCookies(response, SecurityConstants.CookieName.ACCESS, SecurityConstants.CookieName.REFRESH);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "요청 IP가 일치하지 않습니다. 재로그인이 필요합니다.");
			return;
		}

		String oauthId = jwtUtil.getUsername(token);
		String role = jwtUtil.getRole(token);
		log.debug("[JWTFilter] 토큰 인증 - 인증된 사용자 oauthId = {}, role = {}", oauthId, role);

		OAuth2UserDto userDto = OAuth2UserDto.builder()
				.oauthId(oauthId)
				.role(role)
				.build();

		CustomOAuth2User customUser = new CustomOAuth2User(userDto);
		Authentication authToken = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authToken);
	}
}