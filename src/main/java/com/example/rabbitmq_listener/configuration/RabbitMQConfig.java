package com.example.rabbitmq_listener.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

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
    @Bean(name = "customRabbitListenerContainerFactory")
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer> customRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        logger.info("Using customRabbitListenerContainerFactory...");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(properties.getConcurrentConsumers());
        factory.setMaxConcurrentConsumers(properties.getMaxConcurrentConsumers());
        return factory;
    }
}
