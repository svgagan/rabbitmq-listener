package com.example.rabbitmq_listener.listener;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BatchMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(BatchMessageListener.class);

    // This batchListener reads from `queue.fault` and does manual acknowledgement.
    @RabbitListener(ackMode = "#{rabbitMQProperties.acknowledgeMode}", queues = "${queue.fault}", containerFactory = "customRabbitListenerContainerFactory")
    public void receiveBatchMessages(List<Message> messages, Channel channel) {

        long deliveryTag = messages.getLast().getMessageProperties().getDeliveryTag();
        try {
            logger.info("Received batch: {} using thread: {}", messages.size(), Thread.currentThread());

            // Business logic for message transformations and saving it onto database
            Thread.sleep(2000);

            // Manually acknowledge the entire batch
            channel.basicAck(deliveryTag, true);
            logger.info("Batch acknowledged on thread: {}", Thread.currentThread());

        } catch (Exception e) {
            handleBatchError(channel, deliveryTag, e);
        }
    }

    // This batchListener reads from `queue.reading` and does manual acknowledgement.
    @RabbitListener(ackMode = "#{rabbitMQProperties.acknowledgeMode}", queues = "${queue.reading}", containerFactory = "customRabbitListenerContainerFactory")
    public void receiveBatchMessages2(List<Message> messages, Channel channel) {

        long deliveryTag = messages.getLast().getMessageProperties().getDeliveryTag();
        try {
            logger.info("Received batch: {} using thread: {}", messages.size(), Thread.currentThread());

            // Business logic for message transformations and saving it onto database
            Thread.sleep(2000);

            // Manually acknowledge the entire batch
            channel.basicAck(deliveryTag, true);
            logger.info("Batch acknowledged on thread: {}", Thread.currentThread());

        } catch (Exception e) {
            handleBatchError(channel, deliveryTag, e);
        }
    }

    private void handleBatchError(Channel channel, long deliveryTag, Exception e) {
        logger.error("Batch processing failed: {}", e.getMessage());
        try {
            channel.basicNack(deliveryTag, true, true); // Reject batch and requeue messages
        } catch (IOException ioException) {
            logger.error("Error during nack: {}", ioException.getMessage());
        }
    }


}
