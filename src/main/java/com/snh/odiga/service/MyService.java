package com.snh.odiga.service;

import com.snh.odiga.repository.MyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [MyService]
 * <p>
 * 비즈니스 로직 처리를 위한 서비스 클래스.
 * MyRepository를 호출하여 데이터를 처리하고 반환한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyService {

	private final MyRepository myRepository;

	/**
	 * 비즈니스 로직 처리 메서드.
	 *
	 * @return repository로부터 조회한 데이터를 기반으로 처리된 문자열 반환
	 */
	public String processData() {
		// 레파지토리에서 데이터를 조회
		String data = myRepository.findData();
		// 간단한 비즈니스 로직 처리 (예시)
		return "Processed: " + data;
	}
}

