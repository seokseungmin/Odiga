package com.snh.odiga.dto;

/**
 * 클라이언트에게 전달할 Access Token과 Refresh Token 쌍을 표현하는 레코드 클래스.
 * <p>
 * accessToken: 사용자의 인증 및 인가 처리를 위한 JWT Access Token,
 * refreshToken: Access Token 만료 시 재발급을 위한 JWT Refresh Token.
 *
 * @param accessToken  사용자의 인증 및 인가 처리를 위한 JWT Access Token
 * @param refreshToken Access Token 만료 시 재발급을 위한 JWT Refresh Token
 */
public record TokenPair(String accessToken, String refreshToken) {}