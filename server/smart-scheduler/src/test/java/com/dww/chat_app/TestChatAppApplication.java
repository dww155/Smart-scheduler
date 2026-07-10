package com.dww.chat_app;

import org.springframework.boot.SpringApplication;

public class TestChatAppApplication {

	public static void main(String[] args) {
		SpringApplication.from(SmartScheduleApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
