package com.social.api.dto;

public class CreateCommentRequest {

	private Long authorId;

	private String content;

	private Long botId;

	public CreateCommentRequest() {
	}

	public CreateCommentRequest(Long authorId, String content, Long botId) {
		this.authorId = authorId;
		this.content = content;
		this.botId = botId;
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

	public Long getBotId() {
		return botId;
	}

	public void setBotId(Long botId) {
		this.botId = botId;
	}

}
