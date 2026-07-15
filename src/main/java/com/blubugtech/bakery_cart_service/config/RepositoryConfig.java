package com.blubugtech.bakery_cart_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.blubugtech.bakery_cart_service.repository")
@EnableTransactionManagement
public class RepositoryConfig {
    // Additional repository configuration if needed
}
