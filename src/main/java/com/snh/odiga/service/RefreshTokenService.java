package com.snh.odiga.service;

import com.snh.odiga.dto.TokenPair;
import com.snh.odiga.entity.RefreshToken;
import com.snh.odiga.global.exception.InvalidOrExpiredRefreshTokenException;
import com.snh.odiga.repository.RefreshTokenRepository;
import com.snh.odiga.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Refresh Token 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * <p>
 * 토큰 조회, 저장, 삭제, 회전(Token Rotation) 기능을 담당한다.
 */
@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JWTUtil jwtUtil;

	/**
	 * 주어진 리프레시 토큰으로 DB에서 토큰 정보를 조회한다.
	 *
	 * @param refreshToken 조회할 리프레시 토큰 문자열
	 * @return DB에서 조회한 RefreshToken 엔티티 (없으면 Optional.empty())
	 */
	public Optional<RefreshToken> findByRefreshToken(String refreshToken) {
		return refreshTokenRepository.findByRefreshToken(refreshToken);
	}

	/**
	 * DB에서 리프레시 토큰을 삭제한다.
	 *
	 * @param token 삭제할 리프레시 토큰 문자열
	 */
	@Transactional
	public void deleteByRefreshToken(String token) {
		refreshTokenRepository.deleteByRefreshToken(token);
	}

	/**
	 * 새 리프레시 토큰을 DB에 저장한다.
	 *
	 * @param token 저장할 RefreshToken 엔티티
	 */
	@Transactional
	public void save(RefreshToken token) {
		refreshTokenRepository.save(token);
	}

	/**
	 * 기존 리프레시 토큰을 회전시키고 새로운 토큰 쌍(Access Token + Refresh Token)을 생성한다.
	 * <p>
	 * 리프레시 토큰 만료 기간은 7일, 엑세스 토큰은 10분으로 설정된다.
	 *
	 * @param oldToken 기존에 사용된 리프레시 토큰
	 * @param username 사용자 이름
	 * @param oauthId  OAuth 사용자 식별자
	 * @param role     사용자 권한
	 * @param ip       요청자의 IP 주소
	 * @return 새로 발급된 토큰 쌍 (Access Token, Refresh Token)
	 * @throws InvalidOrExpiredRefreshTokenException 기존 토큰이 DB에 존재하지 않을 경우 예외 발생
	 */
	@Transactional
	public TokenPair rotateTokenAndGenerate(String oldToken, String username, String oauthId, String role, String ip) {
		// DB에서 기존 토큰을 조회하여 존재하지 않으면 예외 발생
		refreshTokenRepository.findByRefreshToken(oldToken)
				.orElseThrow(() -> {
					log.warn("[RefreshTokenService] 토큰 회전 실패 - 존재하지 않는 토큰입니다. token={}", oldToken);
					return new InvalidOrExpiredRefreshTokenException();
				});

		// 기존 토큰 하드 삭제
		refreshTokenRepository.deleteByRefreshToken(oldToken);
		log.info("[RefreshTokenService] 기존 토큰 삭제 완료 - token={}", oldToken);

		// 새 토큰 발급 (리프레시 토큰 7일, 엑세스 토큰 10분)
		String newRefreshToken = jwtUtil.createJwt(
				SecurityConstants.TokenCategory.REFRESH, username, oauthId, role, ip, 7 * 24 * 60 * 60L);
		String newAccessToken = jwtUtil.createJwt(
				SecurityConstants.TokenCategory.ACCESS, username, oauthId, role, ip, 60 * 60L);

		// 새 RefreshToken 엔티티 생성 및 DB 저장 (만료 시간은 밀리초 단위)
		RefreshToken newTokenEntity = RefreshToken.builder()
				.refreshToken(newRefreshToken)
				.oauthId(oauthId)
				.expiry(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L))
				.ip(ip)
				.build();
		refreshTokenRepository.save(newTokenEntity);

		log.info("[RefreshTokenService] 새 토큰 발급 완료 - access={}, refresh={}, ip={}",
				newAccessToken, newRefreshToken, ip);

		return new TokenPair(newAccessToken, newRefreshToken);
	}
}