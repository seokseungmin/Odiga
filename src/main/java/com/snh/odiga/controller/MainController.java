package com.snh.odiga.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 메인 페이지 요청을 처리하는 컨트롤러이다.
 */
@RestController
public class MainController {

	/**
	 * "/" 경로에 대한 GET 요청을 처리하며, 간단한 문자열 응답("main route")을 반환한다.
	 *
	 * @return "main route" 문자열
	 */
	@GetMapping("/")
	public String mainAPI() {
		return "main route";
	}
}