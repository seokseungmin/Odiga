package com.snh.odiga.service;

import com.snh.odiga.dto.*;
import com.snh.odiga.entity.Role;
import com.snh.odiga.entity.User;
import com.snh.odiga.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 로그인 성공 후 사용자 정보를 처리하는 서비스 클래스.
 * 제공자 응답을 가공하여 시스템 사용자로 등록하거나 갱신한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Transactional
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest)
			throws OAuth2AuthenticationException {
		log.info("[CustomOAuth2UserService] OAuth2 로그인 요청 - userRequest: {}", userRequest);

		// [1] 제공자로부터 사용자 정보 조회
		OAuth2User oAuth2User = super.loadUser(userRequest);
		log.info("[CustomOAuth2UserService] OAuth2 응답 데이터 - attributes: {}", oAuth2User.getAttributes());

		// [2] 소셜 제공자 식별 (네이버, 구글 등)
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		// [3] 제공자의 응답 데이터를 기반으로 커스텀 응답 객체로 변환
		OAuth2Response oAuth2Response;
		if ("naver".equals(registrationId)) {
			oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
		} else if ("google".equals(registrationId)) {
			oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
		} else {
			log.warn("[CustomOAuth2UserService] 지원하지 않는 OAuth2 제공자 - registrationId: {}", registrationId);
			return null;
		}

		// [4] 고유한 oauthId 생성 (예: google:123456)
		String oauthId = oAuth2Response.getProvider() + ":" + oAuth2Response.getProviderId();
		log.info("[CustomOAuth2UserService] OAuth 사용자 고유 ID 생성 - oauthId: {}", oauthId);

		// [5] DB에서 기존 회원 여부 확인
		User existData = userRepository.findByOauthId(oauthId);

		if (existData == null) {
			// [5-1] 신규 회원인 경우, 사용자 정보를 DB에 저장한다.
			User newUser = User.builder()
					.email(oAuth2Response.getEmail())
					.name(oAuth2Response.getName())
					.oauthId(oauthId)
					.role(Role.USER)
					.build();
			User savedUser = userRepository.save(newUser);
			log.info("[CustomOAuth2UserService] 신규 회원 저장 완료 - {}", savedUser);

			OAuth2UserDto userDto = OAuth2UserDto.builder()
					.oauthId(oauthId)
					.name(oAuth2Response.getName())
					.role(Role.USER.name())
					.build();
			return new CustomOAuth2User(userDto);
		} else {
			// [5-2] 기존 회원인 경우, 최신 정보를 반영하여 DB 정보를 갱신한다.
			existData.setEmail(oAuth2Response.getEmail());
			existData.setName(oAuth2Response.getName());
			log.info("[CustomOAuth2UserService] 기존 회원 정보 업데이트 - {}", existData);

			OAuth2UserDto userDto = OAuth2UserDto.builder()
					.oauthId(existData.getOauthId())
					.name(oAuth2Response.getName()) // 최신 응답 기반으로 저장
					.role(existData.getRole().name())
					.build();
			return new CustomOAuth2User(userDto);
		}
	}
}