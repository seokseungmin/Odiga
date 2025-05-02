package com.snh.odiga.oauth2;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.stereotype.Component;

/**
 * 소셜 로그인(OAuth2) 클라이언트 등록 정보를 설정하는 컴포넌트 클래스.
 * <p>
 * Google, Naver 등 외부 OAuth2 제공자에 대한 등록 정보를 제공한다.
 */
@Component
public class SocialClientRegistration {

	@Value("${social.naver.client-id}")
	private String naverClientId;

	@Value("${social.naver.client-secret}")
	private String naverClientSecret;


	@Value("${social.google.client-id}")
	private String googleClientId;

	@Value("${social.google.client-secret}")
	private String googleClientSecret;


	/**
	 * 네이버 OAuth2 클라이언트 등록 정보를 반환한다.
	 *
	 * @return ClientRegistration - 네이버 설정 정보
	 */
	public ClientRegistration naverClientRegistration() {
		return ClientRegistration
				.withRegistrationId("naver")               // 클라이언트 식별용 등록 ID
				.clientId(naverClientId)            // 네이버에서 발급받은 클라이언트 ID
				.clientSecret(naverClientSecret)                  // 네이버에서 발급받은 클라이언트 시크릿
				.redirectUri("http://localhost:8080/login/oauth2/code/naver")  // 네이버 로그인 성공 후 리다이렉트될 URI
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // OAuth2 인증 방식 (Authorization Code 방식)
				.scope("name", "email")                      // 사용자에게 요청할 정보 범위(scope)
				.authorizationUri("https://nid.naver.com/oauth2.0/authorize")     // 사용자 인증을 위한 네이버 인증 페이지 URI
				.tokenUri("https://nid.naver.com/oauth2.0/token")                  // 액세스 토큰 발급을 위한 네이버 토큰 발급 URI
				.userInfoUri("https://openapi.naver.com/v1/nid/me")                // 사용자 정보 요청 URI
				.userNameAttributeName("response")           // 사용자 정보에서 식별자로 사용할 JSON 키 (네이버는 "response" 아래에 실제 데이터 존재)
				.build();
	}

	/**
	 * 구글 OAuth2 클라이언트 등록 정보를 반환한다.
	 *
	 * @return ClientRegistration - 구글 설정 정보
	 */
	public ClientRegistration googleClientRegistration() {
		return ClientRegistration
				.withRegistrationId("google")              // 클라이언트 식별용 등록 ID
				.clientId(googleClientId) // 구글 클라이언트 ID
				.clientSecret(googleClientSecret)  // 구글 클라이언트 시크릿
				.redirectUri("http://localhost:8080/login/oauth2/code/google")  // 로그인 성공 시 리다이렉트될 URI
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // OAuth2 인증 방식 (Authorization Code 방식)
				.scope("profile", "email")                   // 사용자에게 요청할 정보 범위 (프로필, 이메일)
				.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth") // 구글 인증 서버 URI
				.tokenUri("https://www.googleapis.com/oauth2/v4/token")           // 액세스 토큰 발급 요청 URI
				.jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")            // JWT 서명 검증용 공개 키(JWK) URI
				.issuerUri("https://accounts.google.com")      // 토큰 발급자(issuer)의 URI
				.userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")      // 사용자 정보 요청 URI
				.userNameAttributeName(IdTokenClaimNames.SUB)    // 사용자 식별자로 사용할 필드 (sub: subject, 고유 ID)
				.build();
	}
}