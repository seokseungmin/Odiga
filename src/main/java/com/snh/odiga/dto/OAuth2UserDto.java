package com.snh.odiga.dto;

import lombok.*;

/**
 * OAuth2 인증 후 사용자 정보를 담기 위한 DTO 클래스.
 * <p>
 * 인증된 사용자로부터 전달받은 이름, 역할, OAuth 식별자를 포함한다.
 */
@Getter
@Setter
@Builder
public class OAuth2UserDto {

	/** 사용자 권한 (예: USER, ADMIN) */
	private String role;

	/** 사용자 이름 */
	private String name;

	/** OAuth 제공자에서 발급한 고유 식별자 */
	private String oauthId;
}