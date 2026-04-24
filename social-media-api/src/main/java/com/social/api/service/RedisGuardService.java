package com.social.api.service;

import java.util.List;
import java.util.Set;

public interface RedisGuardService {
	
	public void incrementViralityScore(Long postId,int points);
	
	public Long getViralityScore(Long postId);
	
	public boolean canBotReply(Long postId);
	
	public void incrementBotCount(Long postId);
	
	public Long getBotCount(Long postId);
	
	public boolean isValidDepth(Integer  depth);
	
	public boolean isCooldownActive(Long botId, Long humanId);

	public void setCooldown(Long botId, Long humanId);
	
	public boolean hasRecentNotification(Long userId);
	
	public void setNotificationThrottle(Long userId);
	
	public void addPendingNotification(Long userId, String message);
	
	public List<Object> getPendingNotifications(Long userId);
	
	public void clearPendingNotifications(Long userId);
	
	public Long getPendingNotificationCount(Long userId);
	
	public Set<String> getUsersWithPendingNotifications();
	
	public Long getUserIdFromKey(String key);
}
