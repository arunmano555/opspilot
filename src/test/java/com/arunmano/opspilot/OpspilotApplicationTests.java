package com.arunmano.opspilot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Dummy key so the context starts without GEMINI_API_KEY (e.g. in CI). No API calls are made.
@SpringBootTest(properties = "spring.ai.google.genai.api-key=test-key")
class OpspilotApplicationTests {

	@Test
	void contextLoads() {
	}

}
