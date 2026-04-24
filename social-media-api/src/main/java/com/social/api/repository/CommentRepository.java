package com.social.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.social.api.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	@Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = ?1 AND c.authorId IN (SELECT b.id FROM Bot b)")
	Long countBotCommentsByPostId(Long postId);

	Integer countByPostIdAndAuthorId(Long postId, Long authorId);
}
