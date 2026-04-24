package com.social.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "comments")
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "post_id", nullable = false)
	private Long postId;

	@Column(name = "author_id", nullable = false)
	private Long authorId;

	@Column(name = "content", nullable = false, length = 1000)
	private String content;

	@Column(name = "depth_level", nullable = false)
	private Integer depthLevel = 0;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Comment() {
	}

	public Comment(Long id, Long postId, Long authorId, String content, Integer depthLevel, LocalDateTime createdAt) {
		this.id = id;
		this.postId = postId;
		this.authorId = authorId;
		this.content = content;
		this.depthLevel = depthLevel;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
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

	public Integer getDepthLevel() {
		return depthLevel;
	}

	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}