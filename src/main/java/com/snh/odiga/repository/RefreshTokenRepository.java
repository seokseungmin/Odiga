package com.snh.odiga.repository;

import com.snh.odiga.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * RefreshToken 엔티티를 위한 JPA 레포지토리 인터페이스.
 * <p>
 * 토큰 문자열을 기준으로 조회 및 삭제하는 메서드를 제공한다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

	/**
	 * Refresh Token 문자열을 기반으로 엔티티를 조회한다.
	 *
	 * @param token 조회할 Refresh Token 문자열
	 * @return 해당 토큰에 대한 Optional 래핑된 RefreshToken 엔티티
	 */
	Optional<RefreshToken> findByRefreshToken(String token);

	/**
	 * 주어진 Refresh Token 문자열에 해당하는 엔티티를 삭제한다.
	 *
	 * @param token 삭제할 Refresh Token 문자열
	 */
	void deleteByRefreshToken(String token);
}