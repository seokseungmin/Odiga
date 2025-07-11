package com.snh.odiga.controller;

import com.snh.odiga.service.MyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [MyController]
 * <p>
 * 인가 작업이 정상적으로 작동하는지 확인하기 위한 간단한 테스트 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
public class MyController {

	private final MyService myService;

	/**
	 * "/my" 경로에 대한 GET 요청을 처리하며, 간단한 문자열 응답("my route")을 반환한다.
	 * MyService를 호출하여 서비스를 통한 데이터 처리 결과를 함께 확인할 수 있다.
	 *
	 * @return "my route: <서비스 처리 결과>" 문자열
	 */
	@GetMapping("/my")
	public String myAPI() {
		// MyService의 비즈니스 로직 호출 (AOP 적용 시 로그가 출력될 수 있음)
		String result = myService.processData();
		return "my route: " + result;
	}
}