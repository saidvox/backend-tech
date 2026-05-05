package com.techstore.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.datasource.auto-detect=false")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
