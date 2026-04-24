package com.social.api.dto;

public class CreatePostRequest {
	private Long authorId;

	private String content;

	public CreatePostRequest() {
	}

	public CreatePostRequest(Long authorId, String content) {
		this.authorId = authorId;
		this.content = content;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
