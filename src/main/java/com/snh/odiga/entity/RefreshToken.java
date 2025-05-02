package com.snh.odiga.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * RefreshToken 엔티티 클래스.
 * <p>
 * 사용자의 리프레시 토큰 정보를 저장하는 엔티티 클래스이다.
 * 이 클래스는 토큰 자체를 PK로 사용하여 중복 발급을 방지하며, 보안 강화를 위해
 * 토큰 재사용 방지, 클라이언트 IP 추적, 토큰 만료 시간 관리 등의 기능을 포함한다.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "refresh_token")
public class RefreshToken {

	/**
	 * 리프레시 토큰 자체를 ID(PK)로 사용하여 저장한다.
	 * JWT는 일반적으로 200~500자 사이이며, 넉넉하게 512자로 설정한다.
	 */
	@Id
	@Column(length = 512)
	private String refreshToken;

	/**
	 * OAuth2 제공자로부터 받은 유저 고유 식별자.
	 */
	private String oauthId;

	/**
	 * 토큰 만료 시각 (UNIX timestamp 기준).
	 */
	private Long expiry;

	/**
	 * 토큰 발급 당시 클라이언트의 IP 주소.
	 * 탈취 방지를 위한 추가 정보로 활용된다.
	 */
	private String ip;

	/**
	 * 토큰 생성 시각.
	 */
	@Column(name = "createdAt", updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt = LocalDateTime.now();
}