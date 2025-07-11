package com.snh.odiga.entity;

/**
 * JWT 토큰의 종류를 정의한 열거형(Enum)이다.
 * <p>
 * ACCESS: 사용자 인증을 위한 짧은 수명의 토큰, REFRESH: Access 토큰 재발급을 위한 긴 수명의 토큰.
 */
public enum Token {

	/** 인증 요청 시 사용되는 Access Token */
	access,

	/** Access Token 재발급을 위한 Refresh Token */
	refresh
}