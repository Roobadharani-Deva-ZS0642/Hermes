package org.intics.hermes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HermesApplication {
    public static final String API_V_2 = "/api/v2/";

    public static void main(String[] args) {
        SpringApplication.run(HermesApplication.class, args);
    }

}