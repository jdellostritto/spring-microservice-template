package com.flipfoundry.tutorial.application;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.flipfoundry.tutorial.application.web.controller.DepartController;
import com.flipfoundry.tutorial.application.web.controller.GreetingController;

@SpringBootTest
class SmokeTests {

	@Autowired
	private GreetingController gController;
	@Autowired
	private DepartController dController;

	@Test
	public void contextLoads() throws Exception {
		assertThat(gController).isNotNull();
		assertThat(dController).isNotNull();
	}
}
