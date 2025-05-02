package com.snh.odiga.repository;

import org.springframework.stereotype.Repository;

/**
 * [MyRepository]
 * <p>
 * 간단한 데이터 접근을 위한 레파지토리 클래스.
 * 실제 DB 연동 대신 테스트용 데이터를 반환한다.
 */
@Repository
public class MyRepository {

	/**
	 * 간단한 데이터 조회 메서드.
	 *
	 * @return "data from repository" 문자열 반환
	 */
	public String findData() {
		// 실제 데이터베이스에서 데이터를 조회하는 로직 대신 예시 데이터를 반환
		return "data from repository";
	}
}