package com.leavemanagment.leave_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// this is telling java: hey i am using spring boot, please prepare everything for me
public class LeaveAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeaveAppApplication.class, args);
	}

}
