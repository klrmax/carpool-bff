package com.carpool.demo.config;

import com.carpool.demo.data.api.NotificationQueueManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class NotificationWorkerConfig {

    @Autowired
    private NotificationQueueManager notificationQueue;  // ← Interface!

    private static final Logger logger = Logger.getLogger("NotificationWorker");

    @Scheduled(fixedDelay = 500)
    public void processNotifications() {
        Map<String, Object> notification = notificationQueue.dequeue();

        if (notification != null) {
            logger.info("Notification für User " + notification.get("userId")
                    + ": " + notification.get("message"));

            notificationQueue.markAsRead((Long) notification.get("id"));
        }
    }
}