package com.snh.odiga.oauth2;

import com.snh.odiga.dto.CustomOAuth2User;
import com.snh.odiga.entity.RefreshToken;
import com.snh.odiga.entity.Token;
import com.snh.odiga.repository.RefreshTokenRepository;
import com.snh.odiga.util.CookieUtil;
import com.snh.odiga.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * OAuth2 로그인 성공 후 처리를 담당하는 성공 핸들러 클래스.
 * <p>
 * 사용자 정보를 추출하고, JWT 토큰 발급, RefreshToken DB 저장, 쿠키 설정, 리다이렉션 처리를 수행한다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JWTUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) throws IOException {

		// [1] OAuth2 로그인 후 인증 객체에서 사용자 정보 추출
		CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

		// [2] 사용자 식별 정보 추출: OAuth ID, 사용자 이름, 요청자 IP
		String oauthId = customUserDetails.getOauthId();
		String name = customUserDetails.getName();
		String currentIp = request.getRemoteAddr();

		// [3] 사용자 권한(Role) 추출
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
		String role = "";
		if (iterator.hasNext()) {
			role = iterator.next().getAuthority();
		}

		// [4] AccessToken 및 RefreshToken 생성 (AccessToken: 10분, RefreshToken: 7일)
		String accessToken = jwtUtil.createJwt(Token.access.name(), name, oauthId, role, currentIp, 60 * 10L);
		String refreshToken = jwtUtil.createJwt(Token.refresh.name(), name, oauthId, role, currentIp, 7 * 24 * 60 * 60L);

		// [5] RefreshToken 정보를 DB에 저장 (IP도 함께 저장)
		RefreshToken tokenEntity = RefreshToken.builder()
				.oauthId(oauthId)
				.refreshToken(refreshToken)
				.expiry(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L))
				.ip(currentIp)
				.build();
		refreshTokenRepository.save(tokenEntity);

		// [6] 쿠키 설정 (AccessToken: 10분, RefreshToken: 1일)
		ResponseCookie accessCookie = CookieUtil.createResponseCookie(SecurityConstants.CookieName.ACCESS, accessToken, 600);
		ResponseCookie refreshCookie = CookieUtil.createResponseCookie(SecurityConstants.CookieName.REFRESH, refreshToken, 86400);

		// [7] 발급된 토큰 로그 출력 (보안상 운영 환경에서는 제거 필요)
		log.info("[CustomSuccessHandler] 발급된 AccessToken: '{}'", accessToken);
		log.info("[CustomSuccessHandler] 발급된 RefreshToken: '{}'", refreshToken);

		// [8] 클라이언트에 쿠키로 토큰 전송
		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());

		// [9] 로그인 후 클라이언트(React 등) URI로 리다이렉트
		response.setStatus(HttpStatus.OK.value());
		response.sendRedirect("http://localhost:3000/");
	}
}