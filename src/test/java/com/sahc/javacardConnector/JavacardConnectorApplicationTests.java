package com.sahc.javacardConnector;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JavacardConnectorApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void teste() {
		JavacardService novo = new JavacardService();
		novo.readAndSendCAP();
	}

}
