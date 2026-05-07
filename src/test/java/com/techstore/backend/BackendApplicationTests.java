package com.techstore.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.datasource.auto-detect=false",
		"spring.profiles.active=test"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
