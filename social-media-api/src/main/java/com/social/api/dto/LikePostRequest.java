package com.social.api.dto;

public class LikePostRequest {
	private Long userId;

	public LikePostRequest() {
	}

	public LikePostRequest(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	
}
