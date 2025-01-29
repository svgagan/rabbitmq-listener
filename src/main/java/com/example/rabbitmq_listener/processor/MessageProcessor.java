package com.example.rabbitmq_listener.processor;

@FunctionalInterface
public interface MessageProcessor {
    void processMessage(String message) throws Exception;
}
