package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.NotificationQueueManager;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationQueueImpl implements NotificationQueueManager {

    private Queue<Map<String, Object>> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void enqueue(Integer userId, String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("id", System.nanoTime());
        queue.add(notification);
    }

    @Override
    public Map<String, Object> dequeue() {
        return queue.poll();
    }

    @Override
    public void markAsRead(Long id) {
        // Optional
    }
}