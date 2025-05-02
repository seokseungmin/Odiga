package com.snh.odiga.config;

import com.snh.odiga.jwt.CustomLogoutFilter;
import com.snh.odiga.jwt.JWTFilter;
import com.snh.odiga.oauth2.CustomClientRegistrationRepository;
import com.snh.odiga.oauth2.CustomOAuth2AuthorizedClientService;
import com.snh.odiga.oauth2.CustomSuccessHandler;
import com.snh.odiga.service.CustomOAuth2UserService;
import com.snh.odiga.service.LogoutService;
import com.snh.odiga.service.RefreshTokenService;
import com.snh.odiga.trace.LogTrace;
import com.snh.odiga.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

/**
 * Spring Security 설정 클래스.
 * <p>
 * JWT 기반 인증, OAuth2 소셜 로그인, CORS 설정, 경로별 인가 정책, 필터 체인 구성 등을 포함한다.
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

	// OAuth2 관련 사용자 서비스 및 성공 핸들러
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomSuccessHandler customSuccessHandler;

	// JWT 관련 컴포넌트 및 Refresh Token 관련 서비스, 로그아웃 서비스
	private final JWTUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;
	private final LogoutService logoutService;

	// 소셜 로그인 클라이언트 등록 관련 서비스
	private final CustomClientRegistrationRepository customClientRegistrationRepository;
	private final CustomOAuth2AuthorizedClientService customOAuth2AuthorizedClientService;
	private final JdbcTemplate jdbcTemplate;

	/**
	 * 권한 계층(Role Hierarchy) 설정.
	 * <p>
	 * [SecurityConfig] - ADMIN 권한이 USER 권한을, USER 권한이 ANONYMOUS 권한을 포함하도록 설정한다.
	 *
	 * @return RoleHierarchy 객체
	 */
	@Bean
	public RoleHierarchy roleHierarchy() {
		return RoleHierarchyImpl.withDefaultRolePrefix()
				.role("ADMIN").implies("USER")
				.role("USER").implies("ANONYMOUS")
				.build();
	}

	/**
	 * 정적 리소스 등에 대해 Spring Security 필터를 무시하도록 설정한다.
	 * <p>
	 * [SecurityConfig] - favicon, error, vendor 등 특정 경로는 보안 필터 제외.
	 *
	 * @return WebSecurityCustomizer 객체
	 */
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers(
				"/favicon.ico",
				"/_ignition/**",
				"/error",
				"/vendor/**"
		);
	}

	/**
	 * SecurityFilterChain을 구성한다.
	 * <p>
	 * [SecurityConfig] - CORS, CSRF, 세션, 필터 체인, OAuth2 로그인, 인가 정책 등을 설정한다.
	 *
	 * @param http HttpSecurity 객체
	 * @return 구성된 SecurityFilterChain 객체
	 * @throws Exception 설정 시 발생할 수 있는 예외
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				// CORS 설정: 프론트엔드(React 등) 클라이언트와 통신 허용
				.cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
					@Override
					public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
						CorsConfiguration configuration = new CorsConfiguration();
						configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
						configuration.setAllowedMethods(Collections.singletonList("*"));
						configuration.setAllowCredentials(true);
						configuration.setAllowedHeaders(Collections.singletonList("*"));
						configuration.setMaxAge(3600L);
						// 노출 헤더 설정
						configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
						configuration.setExposedHeaders(Collections.singletonList("Authorization"));
						return configuration;
					}
				}))

				// CSRF 보호 비활성화 (JWT 기반 인증이므로 필요 없음)
				.csrf(csrf -> csrf.disable())

				// 기본 폼 로그인, HTTP Basic 인증 비활성화
				.formLogin(form -> form.disable())
				.httpBasic(httpBasic -> httpBasic.disable())

				// OAuth2 로그인 필터 이후에 JWT 인증 필터 추가
				.addFilterAfter(new JWTFilter(jwtUtil, refreshTokenService), OAuth2LoginAuthenticationFilter.class)

				// 로그아웃 필터 앞에 커스텀 로그아웃 필터 추가
				.addFilterBefore(new CustomLogoutFilter(jwtUtil, logoutService), LogoutFilter.class)

				// OAuth2 로그인 설정
				.oauth2Login(oauth2 -> oauth2
						// 인메모리 방식 소셜 로그인 정보 등록
						.clientRegistrationRepository(customClientRegistrationRepository.clientRegistrationRepository())
						// DB에 소셜 로그인 정보 등록
						.authorizedClientService(customOAuth2AuthorizedClientService.oAuth2AuthorizedClientService(jdbcTemplate, customClientRegistrationRepository.clientRegistrationRepository()))
						// 사용자 정보 획득을 위한 서비스 설정
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
						// 로그인 성공 시 처리 핸들러 설정
						.successHandler(customSuccessHandler))

				// 인가 정책 설정
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/",
								"/favicon.ico",
								"/css/**",
								"/js/**",
								"/images/**",
								"/webjars/**",
								"/ping.js",
								"/reissue/**"
						).permitAll()
						.requestMatchers("/vendor/**").denyAll()
						.requestMatchers("/my/**").hasAuthority("USER")
						.anyRequest().authenticated())

				// 세션 상태 관리: JWT 기반 인증이므로 Stateless하게 처리
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
}