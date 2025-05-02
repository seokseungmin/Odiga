package com.snh.odiga.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * OAuth2 클라이언트 등록 정보를 구성하는 클래스.
 * <p>
 * 구글, 네이버 등 OAuth2 공급자 정보를 애플리케이션 설정에서 가져와
 * Spring Security에서 사용할 수 있는 {@link ClientRegistrationRepository} 형태로 구성하여 반환한다.
 * <p>
 * 이 정보는 정적인 값이므로, 메모리 기반 저장소인 {@link InMemoryClientRegistrationRepository}를 사용한다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CustomClientRegistrationRepository {

	private final SocialClientRegistration socialClientRegistration;

	/**
	 * OAuth2 공급자(ClientRegistration) 정보를 등록한다.
	 * <p>
	 * [CustomClientRegistrationRepository] - client-id, client-secret, redirect-uri 등의 정보는 변경되지 않는 정적 설정이므로
	 * InMemory 방식으로 등록하여 Spring Security가 소셜 로그인 시 필요한 정보를 참조할 수 있도록 한다.
	 *
	 * @return InMemoryClientRegistrationRepository 객체
	 */

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		return new InMemoryClientRegistrationRepository(
				socialClientRegistration.naverClientRegistration(),
				socialClientRegistration.googleClientRegistration());
	}
}