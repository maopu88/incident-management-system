package com.maopu.incident.management;

import com.maopu.incident.management.response.Response;
import com.maopu.incident.management.response.ResponseFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.GetMapping;


@EnableCaching
@SpringBootApplication
public class IncidentManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(IncidentManagementApplication.class, args);
	}


	@GetMapping("/smoke")
	public Response<String> smoke() {
		return ResponseFactory.getSuccess();
	}
}
