package com.maquigest.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Bootstrap class for the MaquiGest Platform application.
 *
 * <p>Initializes Spring Boot auto-configuration and JPA auditing infrastructure.</p>
 */
@EnableJpaAuditing
@SpringBootApplication
public class MaquigestPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaquigestPlatformApplication.class, args);
    }

}
