package com.example.rabbitmq_listener.processor.impl;

import com.example.rabbitmq_listener.processor.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TestLocoMessageImpl implements MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TestLocoMessageImpl.class);
    @Override
    public void processMessage(String message) throws Exception {
        logger.info("Processing Locomotive Message: {}", message);
        // Business logic for message transformations and saving it onto database
    }
}
