package com.snh.odiga.config;

import com.snh.odiga.trace.LogTrace;
import com.snh.odiga.trace.ThreadLocalLogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TraceConfig {

	@Bean
	public LogTrace logTrace() {
		return new ThreadLocalLogTrace();
	}
}