package com.snh.odiga.dto;


import lombok.RequiredArgsConstructor;
import java.util.Map;

/**
 * 구글 OAuth2 로그인 응답을 처리하기 위한 클래스.
 * <p>
 * OAuth2Response 인터페이스를 구현하며,
 * 사용자 정보(JSON 응답)에서 필요한 값을 추출하여 반환한다.
 */
@RequiredArgsConstructor
public class GoogleResponse implements OAuth2Response {

	/** 구글 응답에서 받은 데이터가 저장된 Map */
	private final Map<String, Object> attribute;

	/**
	 * OAuth2 공급자 이름을 반환한다.
	 *
	 * @return 공급자 이름 "google"
	 */
	@Override
	public String getProvider() {
		return "google";
	}

	/**
	 * 구글에서 발급한 사용자 고유 ID(sub)를 반환한다.
	 *
	 * @return 제공자 고유 ID
	 */
	@Override
	public String getProviderId() {
		return attribute.get("sub").toString();
	}

	/**
	 * 사용자의 이메일 주소를 반환한다.
	 *
	 * @return 이메일 주소
	 */
	@Override
	public String getEmail() {
		return attribute.get("email").toString();
	}

	/**
	 * 사용자의 이름을 반환한다.
	 *
	 * @return 사용자 이름
	 */
	@Override
	public String getName() {
		return attribute.get("name").toString();
	}
}