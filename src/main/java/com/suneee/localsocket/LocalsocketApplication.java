package com.suneee.localsocket;

import com.suneee.localsocket.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LocalsocketApplication implements CommandLineRunner {
	@Autowired
	private InitService initService;

	public static void main(String[] args) {
		SpringApplication.run(LocalsocketApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//加载配置到内存,启动RabbitMQ监听服务
		initService.build();
	}
}
