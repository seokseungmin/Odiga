package com.snh.odiga.dto;


import java.util.Map;

/**
 * 네이버 OAuth2 로그인 응답을 처리하기 위한 DTO 클래스.
 * <p>
 * 네이버에서 반환된 사용자 정보를 파싱하여 필요한 값을 반환한다.
 * <p>
 * 응답 예시:
 * {
 *   "resultcode": "00",
 *   "message": "success",
 *   "response": {
 *     "id": "123123123",
 *     "email": "user@example.com",
 *     "name": "홍길동"
 *   }
 * }
 */
public class NaverResponse implements OAuth2Response {

	/**
	 * 네이버 응답에서 "response" 객체 부분을 파싱하여 저장한 맵.
	 */
	private final Map<String, Object> attribute;

	/**
	 * 생성자.
	 * <p>
	 * [NaverResponse] - 네이버 응답 전체 Map에서 "response" 키에 해당하는 값을 파싱하여 저장한다.
	 *
	 * @param attribute 네이버 응답 전체를 담은 Map
	 */
	public NaverResponse(Map<String, Object> attribute) {
		this.attribute = (Map<String, Object>) attribute.get("response");
	}

	/**
	 * OAuth2 제공자 이름을 반환한다.
	 *
	 * @return "naver"
	 */
	@Override
	public String getProvider() {
		return "naver";
	}

	/**
	 * 네이버에서 발급한 고유 사용자 ID를 반환한다.
	 *
	 * @return 사용자 ID
	 */
	@Override
	public String getProviderId() {
		return attribute.get("id").toString();
	}

	/**
	 * 사용자 이메일을 반환한다.
	 *
	 * @return 사용자 이메일 주소
	 */
	@Override
	public String getEmail() {
		return attribute.get("email").toString();
	}

	/**
	 * 사용자 이름(닉네임 또는 실명)을 반환한다.
	 *
	 * @return 사용자 이름
	 */
	@Override
	public String getName() {
		return attribute.get("name").toString();
	}
}