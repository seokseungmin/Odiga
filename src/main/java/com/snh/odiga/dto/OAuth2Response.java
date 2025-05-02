package com.snh.odiga.dto;


/**
 * OAuth2 인증 제공자로부터 받은 사용자 정보를 공통 인터페이스로 추상화한 클래스.
 * <p>
 * Google, Naver 등 다양한 OAuth2 제공자의 응답 데이터를 표준화하는 데 사용된다.
 */
public interface OAuth2Response {

	/**
	 * OAuth2 제공자 이름을 반환한다. (예: google, naver)
	 *
	 * @return 제공자 이름
	 */
	String getProvider();

	/**
	 * 제공자로부터 발급받은 고유 사용자 식별자를 반환한다.
	 *
	 * @return 고유 사용자 식별자
	 */
	String getProviderId();

	/**
	 * 사용자 이메일을 반환한다.
	 *
	 * @return 사용자 이메일 주소
	 */
	String getEmail();

	/**
	 * 사용자 이름(실명 또는 닉네임 등)을 반환한다.
	 *
	 * @return 사용자 이름
	 */
	String getName();
}