package com.snh.odiga.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * OAuth2 인증 정보를 담는 사용자 구현 클래스.
 * <p>
 * OAuth2UserDto를 기반으로 사용자 정보를 반환하며,
 * Spring Security에서 사용자 인증 정보로 활용된다.
 */
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

	private final OAuth2UserDto oAuth2UserDto;

	/**
	 * 사용자 속성 정보를 반환한다.
	 *
	 * @return 사용자 속성이 담긴 Map (예: "name", "role")
	 */
	@Override
	public Map<String, Object> getAttributes() {
		return Map.of("name", oAuth2UserDto.getName(), "role", oAuth2UserDto.getRole());
	}

	/**
	 * 사용자 권한 목록을 반환한다.
	 *
	 * @return 사용자의 권한 목록
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(() -> oAuth2UserDto.getRole());
		return authorities;
	}

	/**
	 * 사용자 이름을 반환한다. (OAuth2User의 식별자)
	 *
	 * @return 사용자 이름
	 */
	@Override
	public String getName() {
		return oAuth2UserDto.getName();
	}

	/**
	 * OAuth2 제공자에서 발급한 고유 식별자를 반환한다.
	 *
	 * @return OAuth2 제공자 고유 식별자
	 */
	public String getOauthId() {
		return oAuth2UserDto.getOauthId();
	}
}