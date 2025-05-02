package com.snh.odiga.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * OAuth2 로그인 후 발급받은 토큰 정보를 저장하는 설정 클래스.
 * <p>
 * 사용자가 구글, 네이버 등 소셜 로그인을 할 경우
 * 생성되는 access token, refresh token 등의 인증 관련 정보를 DB에 저장하여
 * 서버 재시작이나 요청 증가 상황에서도 인증 정보를 유지할 수 있도록 설정한다.
 */
@Configuration
public class CustomOAuth2AuthorizedClientService {

	/**
	 * OAuth2 인증 클라이언트 정보를 DB에 저장하는 서비스 Bean을 등록한다.
	 * <p>
	 * [CustomOAuth2AuthorizedClientService] - JdbcTemplate과 ClientRegistrationRepository를 사용하여
	 * Jdbc 기반의 OAuth2AuthorizedClientService 객체를 생성한다.
	 *
	 * @param jdbcTemplate                  JDBC 연동을 위한 JdbcTemplate
	 * @param clientRegistrationRepository  클라이언트 등록 정보를 담은 저장소
	 * @return Jdbc 기반의 OAuth2AuthorizedClientService 객체
	 */
	@Bean
	public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(
			JdbcTemplate jdbcTemplate,
			ClientRegistrationRepository clientRegistrationRepository) {
		return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
	}
}