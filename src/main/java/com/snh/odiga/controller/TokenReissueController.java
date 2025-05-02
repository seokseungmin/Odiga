package com.snh.odiga.controller;

import com.snh.odiga.common.SecurityConstants;
import com.snh.odiga.entity.RefreshToken;
import com.snh.odiga.global.exception.InvalidOrExpiredRefreshTokenException;
import com.snh.odiga.service.RefreshTokenService;
import com.snh.odiga.util.CookieUtil;
import com.snh.odiga.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 클라이언트 쿠키에 저장된 Refresh Token을 기반으로 새로운 Access Token과 Refresh Token을 재발급해주는 컨트롤러.
 * <ul>
 *   <li>유효한 Refresh Token이 존재하면 새 토큰을 생성하여 쿠키에 저장한 후 응답으로 반환</li>
 *   <li>재발급 요청은 인증 없이 접근 가능하며, Refresh Token의 유효성과 카테고리를 검사한다.</li>
 *   <li>Refresh Token의 재사용 방지 및 보안을 위해 DB에 저장하여 관리한다.</li>
 * </ul>
 * [요청 URL] POST /reissue
 * [요청 헤더] 쿠키에 Refresh Token 포함
 * [응답] Access Token, Refresh Token을 Set-Cookie로 전달
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/reissue")
public class TokenReissueController {

	private final JWTUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;
	private static final Logger log = LoggerFactory.getLogger(TokenReissueController.class);

	/**
	 * [0] Refresh Token을 이용해 Access Token과 Refresh Token을 재발급하는 API.
	 * <p>
	 * 유효한 Refresh Token이 쿠키에 존재하면 새 토큰을 발급하여
	 * 쿠키에 저장한 후 응답으로 반환한다.
	 *
	 * @param request  HttpServletRequest 객체
	 * @param response HttpServletResponse 객체
	 * @return 재발급 성공 시 200 OK 응답, 실패 시 401 Unauthorized 응답
	 */
	@PostMapping
	public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
		// [1] 쿠키에서 Refresh Token 추출
		String refreshToken = CookieUtil.getCookieValue(request, SecurityConstants.CookieName.REFRESH);
		if (refreshToken == null || jwtUtil.isExpired(refreshToken)) {
			log.warn("[TokenReissueController] 재발급 실패 - Refresh Token이 없거나 만료됨");
			return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
					.body("Refresh Token이 없거나 만료되었습니다.");
		}

		// [2] 토큰 카테고리 확인
		String category = jwtUtil.getCategory(refreshToken);
		if (!SecurityConstants.TokenCategory.REFRESH.equals(category)) {
			log.warn("[TokenReissueController] 재발급 실패 - 카테고리가 'refresh'가 아님: {}", category);
			return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
					.body("유효하지 않은 Refresh Token입니다.");
		}

		// [3] 토큰 정보에서 사용자 정보 추출 및 요청 IP 획득
		String username = jwtUtil.getUsername(refreshToken);
		String oauthId = jwtUtil.getOauthId(refreshToken);
		String ip = request.getRemoteAddr();

		// [4] DB에서 Refresh Token 존재 여부 확인
		refreshTokenService.findByRefreshToken(refreshToken)
				.orElseThrow(() -> {
					log.warn("[TokenReissueController] 재발급 실패 - DB에 Refresh Token이 없음: {}", refreshToken);
					return new InvalidOrExpiredRefreshTokenException();
				});

		String role = jwtUtil.getRole(refreshToken);

		// [5] 새 토큰 발급 (Access Token: 10분, Refresh Token: 7일)
		String newAccessToken = jwtUtil.createJwt(SecurityConstants.TokenCategory.ACCESS, username, oauthId, role, ip, 10 * 60L);
		String newRefreshToken = jwtUtil.createJwt(SecurityConstants.TokenCategory.REFRESH, username, oauthId, role, ip, 7 * 24 * 60 * 60L);
		log.info("[TokenReissueController] 토큰 재발급 완료 - 사용자: {}, 권한: {}", username, role);

		// [6] 새 Refresh Token 정보를 DB에 저장
		RefreshToken newTokenEntity = RefreshToken.builder()
				.oauthId(oauthId)
				.refreshToken(newRefreshToken)
				.expiry(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L))
				.ip(ip)
				.build();
		refreshTokenService.save(newTokenEntity);

		// [7] 쿠키 갱신 (Access Token: 10분, Refresh Token: 1일)
		ResponseCookie accessCookie = CookieUtil.createResponseCookie(SecurityConstants.CookieName.ACCESS, newAccessToken, 600);
		ResponseCookie refreshCookie = CookieUtil.createResponseCookie(SecurityConstants.CookieName.REFRESH, newRefreshToken, 86400);

		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());

		return ResponseEntity.ok().body("토큰이 재발급되었습니다.");
	}
}