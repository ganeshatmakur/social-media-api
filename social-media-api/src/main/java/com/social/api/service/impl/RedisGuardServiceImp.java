package com.social.api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.social.api.service.RedisGuardService;

@Service
public class RedisGuardServiceImp implements RedisGuardService {

	private final RedisTemplate<String, Object> redisTemplate;

	private static final String POST_PREFIX = "post:";
	private static final String USER_PREFIX = "user:";
	private static final String BOT_COOLDOWN_PREFIX = "cooldown:bot:";
	private static final String NOTIFICATION_THROTTLE_PREFIX = "notification_throttle:user:";

	private static final long BOT_COOLDOWN_MINUTES = 60;
	private static final long NOTIFICATION_THROTTLE_MINUTES = 5;

	private static final int MAX_BOT_REPLIES = 100;
	private static final int MAX_COMMENT_DEPTH = 20;

	public RedisGuardServiceImp(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void incrementViralityScore(Long postId, int points) {
		String key = POST_PREFIX + postId + ":virality_score";
		redisTemplate.opsForValue().increment(key, points);
	}

	@Override
	public Long getViralityScore(Long postId) {
		String key = POST_PREFIX + postId + ":virality_score";
		Object value = redisTemplate.opsForValue().get(key);
		return value != null ? Long.parseLong(value.toString()) : 0L;
	}

	@Override
	public boolean canBotReply(Long postId) {
		String key = POST_PREFIX + postId + ":bot_count";

		Object value = redisTemplate.opsForValue().get(key);
		long count = value != null ? Long.parseLong(value.toString()) : 0L;

		return count < MAX_BOT_REPLIES;
	}

	@Override
	public void incrementBotCount(Long postId) {
		String key = POST_PREFIX + postId + ":bot_count";
		redisTemplate.opsForValue().increment(key);
	}

	@Override
	public Long getBotCount(Long postId) {
		String key = POST_PREFIX + postId + ":bot_count";
		Object value = redisTemplate.opsForValue().get(key);
		return value != null ? Long.parseLong(value.toString()) : 0L;
	}

	@Override
	public boolean isValidDepth(Integer depth) {
		return depth != null && depth <= MAX_COMMENT_DEPTH;
	}

	@Override
	public boolean isCooldownActive(Long botId, Long humanId) {
		String key = BOT_COOLDOWN_PREFIX + botId + ":human:" + humanId;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	@Override
	public void setCooldown(Long botId, Long humanId) {
		String key = BOT_COOLDOWN_PREFIX + botId + ":human:" + humanId;
		redisTemplate.opsForValue().set(key, "active", BOT_COOLDOWN_MINUTES, TimeUnit.MINUTES);
	}

	@Override
	public boolean hasRecentNotification(Long userId) {
		String key = NOTIFICATION_THROTTLE_PREFIX + userId;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	@Override
	public void setNotificationThrottle(Long userId) {
		String key = NOTIFICATION_THROTTLE_PREFIX + userId;
		redisTemplate.opsForValue().set(key, "active", NOTIFICATION_THROTTLE_MINUTES, TimeUnit.MINUTES);
	}

	@Override
	public void addPendingNotification(Long userId, String message) {
		String key = USER_PREFIX + userId + ":pending_notifications";
		redisTemplate.opsForList().rightPush(key, message);
	}

	@Override
	public List<Object> getPendingNotifications(Long userId) {
		String key = USER_PREFIX + userId + ":pending_notifications";
		List<Object> list = redisTemplate.opsForList().range(key, 0, -1);
		return list != null ? list : new ArrayList<>();
	}

	@Override
	public void clearPendingNotifications(Long userId) {
		String key = USER_PREFIX + userId + ":pending_notifications";
		redisTemplate.delete(key);
	}

	@Override
	public Long getPendingNotificationCount(Long userId) {
		String key = USER_PREFIX + userId + ":pending_notifications";
		Long size = redisTemplate.opsForList().size(key);
		return size != null ? size : 0L;
	}

	@Override
	public Set<String> getUsersWithPendingNotifications() {
 		return redisTemplate.keys(USER_PREFIX + "*:pending_notifications");
	}

	@Override
	public Long getUserIdFromKey(String key) {
		String[] parts = key.split(":");
		return Long.parseLong(parts[1]);
	}
}
