package com.mywuwu.gameserver.webserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@SpringBootApplication(scanBasePackages =
        {"com.linkflywind.gameserver.loginserver",
                "com.linkflywind.gameserver.data",
                "com.linkflywind.gameserver.core"
        })
@EnableMongoRepositories("com.linkflywind.gameserver.data")
public class WebServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebServerApplication.class);
    }
}
