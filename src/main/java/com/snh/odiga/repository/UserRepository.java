package com.snh.odiga.repository;

import com.snh.odiga.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 정보를 처리하는 JPA 리포지토리 인터페이스.
 * <p>
 * [UserRepository] - 기본적인 CRUD 기능은 JpaRepository를 통해 제공되며,
 * OAuth ID를 기준으로 사용자를 조회하는 메서드가 추가되어 있다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * OAuth2 인증 과정에서 전달된 고유 oauthId로 사용자 정보를 조회한다.
	 *
	 * @param oauthId OAuth 제공자 및 식별자가 포함된 고유 ID (예: google:12345678)
	 * @return 해당 oauthId를 가진 사용자 엔티티
	 */
	User findByOauthId(String oauthId);
}