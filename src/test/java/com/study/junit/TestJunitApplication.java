package com.study.junit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestJunitApplication {

	@Bean
	@ServiceConnection
	OracleContainer oracleContainer() {
		return new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:latest"));
	}

	public static void main(String[] args) {
		SpringApplication.from(JunitApplication::main).with(TestJunitApplication.class).run(args);
	}

}
