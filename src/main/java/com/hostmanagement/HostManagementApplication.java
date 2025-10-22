package com.hostmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HostManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(HostManagementApplication.class, args);
		System.out.println("\n===========================================");
		System.out.println("Host Management API is running!");
		System.out.println("API Base URL: http://localhost:8080/api");
		System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
		System.out.println("===========================================\n");
	}

}
