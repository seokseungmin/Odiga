package com.snh.odiga.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 정보를 저장하는 엔티티 클래스.
 * <p>
 * OAuth2를 통해 로그인한 사용자 정보를 DB에 저장하는 엔티티이다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
		name = "users",
		indexes = @Index(name = "idx_oauth_id", columnList = "oauthId"),
		uniqueConstraints = @UniqueConstraint(name = "uk_oauth_id", columnNames = "oauthId")
)
public class User {

	/**
	 * 기본 키, 자동 생성되는 사용자 ID.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	/**
	 * OAuth 공급자와 사용자 식별자를 결합한 고유 ID (예: google:12345).
	 */
	@Column(nullable = false)
	private String oauthId;

	/**
	 * 사용자 이름.
	 */
	private String name;

	/**
	 * 사용자 이메일 주소.
	 */
	private String email;

	/**
	 * 사용자 권한 (예: USER, ADMIN).
	 */
	@Enumerated(EnumType.STRING)
	private Role role;

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", oauthId='" + oauthId + '\'' +
				", name='" + name + '\'' +
				", email='" + email + '\'' +
				", role=" + role +
				'}';
	}
}