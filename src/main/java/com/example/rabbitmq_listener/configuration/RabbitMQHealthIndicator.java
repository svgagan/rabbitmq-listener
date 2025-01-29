package com.example.rabbitmq_listener.configuration;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQHealthIndicator implements HealthIndicator {

    private final ConnectionFactory connectionFactory;

    public RabbitMQHealthIndicator(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try {
            // Check if the connection is alive (without opening a new connection)
            if (connectionFactory.createConnection().isOpen()) {
                return Health.up().withDetail("RabbitMQ", "Available").build();
            } else {
                return Health.down().withDetail("RabbitMQ", "Connection is closed").build();
            }
        } catch (Exception e) {
            return Health.down().withDetail("RabbitMQ", "Unavailable: " + e.getMessage()).build();
        }
    }
}
