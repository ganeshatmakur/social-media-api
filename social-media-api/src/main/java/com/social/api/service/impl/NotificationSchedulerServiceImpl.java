package com.social.api.service.impl;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.social.api.service.NotificationSchedulerService;
import com.social.api.service.RedisGuardService;

@Service
@EnableScheduling
public class NotificationSchedulerServiceImpl implements NotificationSchedulerService {
	private final Logger log = LoggerFactory.getLogger(NotificationSchedulerServiceImpl.class);
	private final RedisGuardService redisGuardService;

	public NotificationSchedulerServiceImpl(RedisGuardService redisGuardService) {
		this.redisGuardService = redisGuardService;
	}

	@Override
	@Scheduled(fixedDelay = 300000) // 5 minutes = 300000 ms
	public void processPendingNotification() {

		log.info("[SCHEDULER] Running notification sweep at {}", System.currentTimeMillis());
		Set<String> userKeys = redisGuardService.getUsersWithPendingNotifications();

		if (userKeys == null || userKeys.isEmpty()) {
			log.info("[SCHEDULER] No pending notifications found");
			return;
		}

		for (String userKey : userKeys) {
			Long userId = redisGuardService.getUserIdFromKey(userKey);
			List<Object> pendingNotification = redisGuardService.getPendingNotifications(userId);

			if (pendingNotification != null && !pendingNotification.isEmpty()) {
				int count = pendingNotification.size();
				String message = count > 1
				        ? "Bot X and " + (count - 1) + " others interacted with your post"
				        : "Bot X interacted with your post";

				log.info("Summarized Push Notification: {} " , message);

				redisGuardService.clearPendingNotifications(userId);
			}
		}

	}

}
