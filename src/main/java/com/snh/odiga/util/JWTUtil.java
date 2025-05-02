package com.snh.odiga.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 유틸리티 클래스.
 * 토큰의 발급, 유효성 검사, Claim 추출 등의 기능을 제공한다.
 */
@Component
public class JWTUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(JWTUtil.class);

	private final SecretKey secretKey;

	/**
	 * JWTUtil 생성자.
	 * 애플리케이션 설정에서 비밀키를 초기화한다.
	 *
	 * @param secret 애플리케이션 설정 파일에 정의된 JWT 비밀키
	 */
	public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
		this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
				Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	/**
	 * 토큰에서 사용자 이름을 추출한다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 토큰에 저장된 사용자 이름
	 */
	public String getUsername(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.get("name", String.class);
	}

	/**
	 * 토큰에서 사용자 권한을 추출한다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 토큰에 저장된 사용자 권한
	 */
	public String getRole(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.get("role", String.class);
	}

	/**
	 * 토큰의 만료 여부를 확인한다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 토큰이 만료되었으면 {@code true}, 아니면 {@code false}
	 */
	public Boolean isExpired(String token) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getExpiration()
					.before(new Date());
		} catch (ExpiredJwtException e) {
			LOGGER.info("[JWTUtil] 토큰 만료됨: {}", e.getMessage());
			return true; // 만료된 경우 true 반환
		}
	}

	/**
	 * 토큰에서 OAuth 사용자 ID를 추출한다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 토큰에 저장된 OAuth 사용자 ID
	 */
	public String getOauthId(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.get("oauthId", String.class);
	}

	/**
	 * 토큰의 카테고리를 추출한다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 토큰에 저장된 카테고리
	 */
	public String getCategory(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.get("category", String.class);
	}

	/**
	 * 토큰의 아이피를 추출한다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 토큰에 저장된 아이피 주소
	 */
	public String getIp(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.get("ip", String.class);
	}

	/**
	 * 새로운 JWT 토큰을 생성한다.
	 *
	 * @param category       토큰 카테고리 (예: access, refresh)
	 * @param name           사용자 이름
	 * @param oauthId        OAuth 사용자 ID
	 * @param role           사용자 권한
	 * @param ip             요청자의 아이피 주소
	 * @param expiredSeconds 토큰 만료 시간 (초 단위)
	 * @return 생성된 JWT 토큰 문자열
	 */
	public String createJwt(String category, String name, String oauthId, String role, String ip,
							Long expiredSeconds) {
		return Jwts.builder()
				.claim("category", category)
				.claim("name", name)
				.claim("oauthId", oauthId)
				.claim("role", role)
				.claim("ip", ip)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + expiredSeconds * 1000)) // 초를 밀리초로 변환
				.signWith(secretKey)
				.compact();
	}
}