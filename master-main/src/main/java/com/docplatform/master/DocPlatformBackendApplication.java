package com.docplatform.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DocPlatformBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocPlatformBackendApplication.class, args);
    }

}