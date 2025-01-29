package com.example.rabbitmq_listener.listener;

import com.example.rabbitmq_listener.processor.MessageProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class RabbitMQListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    private final MessageProcessor messageProcessor;
    private final ExecutorService customExecutorService;
    private final ObjectMapper customObjectMapper;

    private static final int BATCH_SIZE = 10; // Number of messages per batch
    private static final int BATCH_MAX_WAIT = 5000; // Runs every 5 seconds to process pending messages

    private final List<Message> messageBuffer = new ArrayList<>(); // Buffer for batch processing
    private Channel lastChannel; // Track the last channel used

    public RabbitMQListener(MessageProcessor messageProcessor, ExecutorService customExecutorService, ObjectMapper customObjectMapper) {
        this.messageProcessor = messageProcessor;
        this.customExecutorService = customExecutorService;
        this.customObjectMapper = customObjectMapper;
    }

    // To handle batch of message within a single thread
    @RabbitListener(containerFactory = "customRabbitListenerContainerFactory", queues = "#{rabbitMQProperties.queue1}", concurrency = "#{rabbitMQProperties.concurrentConsumers}", ackMode = "MANUAL")
    public void consumeMessage(Message message, Channel channel) {

        synchronized (messageBuffer) {
            messageBuffer.add(message);
            lastChannel = channel; // Store the last used channel for batch acknowledgment
        }

        if (messageBuffer.size() >= BATCH_SIZE) {
            processBatch();
        }
    }

    @Scheduled(fixedRate = BATCH_MAX_WAIT) // Runs every 5 seconds to process pending messages
    public void processPendingMessages() {
        processBatch();
    }

    private void processBatch() {
        List<Message> batch;
        synchronized (messageBuffer) {
            if (messageBuffer.isEmpty() || lastChannel == null) return;
            batch = new ArrayList<>(messageBuffer);
            messageBuffer.clear();
        }

        customExecutorService.submit(() -> {
            try {
                processMessages(batch);
                // Acknowledge all messages in batch
                for (Message msg : batch) {
                    lastChannel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                }
            } catch (Exception e) {
                // Reject messages and requeue them
                for (Message msg : batch) {
                    try {
                        lastChannel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
                    } catch (IOException ioException) {
                        logger.error("Failed to nack message: {}", ioException.getMessage());
                    }
                }
            }
        });
    }

    private void processMessages(List<Message> batch) {
        for (Message message : batch) {
            try {
                String messageBody = customObjectMapper.readValue(message.getBody(), String.class);
                messageProcessor.processMessage(messageBody);
                logger.debug("Processed: {}", messageBody);
            } catch (Exception e) {
                logger.error("Failed to process message: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process message: " + e.getMessage(), e);
            }
        }
    }

    // To handle single message within a single thread
    @RabbitListener(queues = "#{rabbitMQProperties.queue2}", concurrency = "#{rabbitMQProperties.concurrentConsumers}", ackMode = "MANUAL", containerFactory = "customRabbitListenerContainerFactory")
    public void consumeSingleMessage(Message message, Channel channel) {
        customExecutorService.submit(() -> {
            try {
                String messageBody = customObjectMapper.readValue(message.getBody(), String.class);
                messageProcessor.processMessage(messageBody);
                System.out.println("Processed: " + messageBody);
            } catch (Exception e) {
                System.err.println("Message processing failed: " + e.getMessage());
                throw new RuntimeException("Failed to process message", e);
            }
        });
    }
}
