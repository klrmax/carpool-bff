package com.carpool.demo.data.api;

import java.util.Map;

public interface NotificationQueueManager {

    void enqueue(Integer userId, String message, String type);

    Map<String, Object> dequeue();

    void markAsRead(Long id);
}