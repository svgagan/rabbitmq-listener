package com.example.rabbitmq_listener.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
@Getter
@Setter
public class RabbitMQProperties {

    private String host;
    private int port;
    private String username;
    private String password;
    private int concurrentConsumers;
    private int maxConcurrentConsumers;
    private boolean enableVirtualThreads;
    private String acknowledgeMode;
    private int batchSize;
    private int batchReceiveTimeout;

    // Getters and Setters
}
