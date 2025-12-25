package com.FindMyService;

import com.FindMyService.config.SecurityConfig;
import com.FindMyService.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = FindMyServiceApplication.class
)
@ComponentScan(
		basePackages = "com.FindMyService",
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = {SecurityConfig.class, JwtAuthFilter.class}
		)
)
@ActiveProfiles("test")
class FindMyServiceApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring application context loads successfully
	}

}
