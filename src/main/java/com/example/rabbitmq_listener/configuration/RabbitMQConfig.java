package com.example.rabbitmq_listener.configuration;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class RabbitMQConfig {

    private final RabbitMQProperties properties;

    public RabbitMQConfig(RabbitMQProperties properties) {
        this.properties = properties;
    }

    // We can use this template for sending message to rabbitmq.
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    // To get virtual (Supported in java 21) or platform thread pool based on flags.
    @Bean(name = "customExecutorService")
    public ExecutorService taskExecutor() {
        return properties.isEnableVirtualThreads()
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(properties.getConcurrentConsumers());
    }

    // Listener factory that will be used for fetching messages from rabbitmq.
    @Bean
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(properties.getConcurrentConsumers());
        factory.setMaxConcurrentConsumers(properties.getMaxConcurrentConsumers());
        return factory;
    }
}
