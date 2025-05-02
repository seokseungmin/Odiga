package com.snh.odiga.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 프론트엔드(React 등)와 백엔드(Spring Boot) 간의 CORS(Cross-Origin Resource Sharing)
 * 문제를 해결하기 위한 설정 클래스.
 * <p>
 * 모든 API 경로("/**")에 대해 CORS를 허용하며, 프론트엔드 주소("http://localhost:3000")에서 오는 요청을 허용한다.
 * 또한 클라이언트에서 'Set-Cookie' 헤더에 접근할 수 있도록 노출한다.
 */
@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

	/**
	 * CORS 정책을 설정한다.
	 *
	 * @param corsRegistry CORS 매핑 등록을 위한 레지스트리 객체
	 */
	@Override
	public void addCorsMappings(CorsRegistry corsRegistry) {
		corsRegistry.addMapping("/**")                     // 모든 경로에 대해 CORS 허용
				.exposedHeaders("Set-Cookie")          // 클라이언트에서 'Set-Cookie' 헤더 접근 허용
				.allowedOrigins("http://localhost:3000");  // 허용할 프론트엔드 origin
	}
}