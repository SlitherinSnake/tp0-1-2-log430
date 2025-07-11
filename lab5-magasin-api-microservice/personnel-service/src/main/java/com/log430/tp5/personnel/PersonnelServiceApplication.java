package com.log430.tp5.personnel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.log430.tp5")
@EnableDiscoveryClient
@EnableCaching
@EntityScan({"com.log430.tp5.domain.personnel"})
@EnableJpaRepositories("com.log430.tp5.infrastructure.repository")
public class PersonnelServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonnelServiceApplication.class, args);
    }
}
