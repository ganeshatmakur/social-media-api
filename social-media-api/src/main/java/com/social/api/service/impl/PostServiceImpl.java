package com.social.api.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.social.api.entity.Comment;
import com.social.api.entity.Like;
import com.social.api.entity.Post;
import com.social.api.repository.CommentRepository;
import com.social.api.repository.LikeRepository;
import com.social.api.repository.PostRepository;
import com.social.api.repository.UserRepository;
import com.social.api.service.PostService;
import com.social.api.service.RedisGuardService;

@Service
public class PostServiceImpl implements PostService {
	private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

	private static final int MAX_COMMENT_DEPTH = 20;
	private static final int MAX_BOT_REPLIES = 100;
	private static final int HUMAN_LIKE_SCORE = 20;

	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final LikeRepository likeRepository;
	private final UserRepository userRepository;
	private final RedisGuardService redisGuardService;

	public PostServiceImpl(PostRepository postRepository, CommentRepository commentRepository,
			LikeRepository likeRepository, UserRepository userRepository, RedisGuardService redisGuardService) {

		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.likeRepository = likeRepository;
		this.userRepository = userRepository;
		this.redisGuardService = redisGuardService;
	}

	@Override
	public Post createPost(Long authorId, String content) {

		log.info("Creating post for authorId={}, contentLength={}", authorId, content.length());

		Post post = new Post();
		post.setAuthorId(authorId);
		post.setContent(content);
		post.setCreatedAt(LocalDateTime.now());

		return postRepository.save(post);
	}

	@Override
	public Comment addComment(Long postId, Long authorId, String content, Long botId) {

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

		int depthLevel = 0;

		if (depthLevel > MAX_COMMENT_DEPTH) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Comment depth exceeds maximum (" + MAX_COMMENT_DEPTH + ")");
		}

		Long postAuthorId = post.getAuthorId();

		if (botId != null && botId > 0) {

			if (!redisGuardService.canBotReply(postId)) {
				throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
						"Bot reply limit exceeded for this post (" + MAX_BOT_REPLIES + ")");
			}

			if (isHumanUser(postAuthorId)) {

				if (redisGuardService.isCooldownActive(botId, postAuthorId)) {
					throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
							"Bot cooldown active for this user (10 min)");
				}

				redisGuardService.setCooldown(botId, postAuthorId);
			}

			redisGuardService.incrementBotCount(postId);
			redisGuardService.incrementViralityScore(postId, 1);

			if (isHumanUser(postAuthorId)) {
				sendNotificationForBotInteraction(postId, postAuthorId, botId);
			}
		}

		Comment comment = new Comment();
		comment.setPostId(postId);
		comment.setAuthorId(authorId);
		comment.setContent(content);
		comment.setDepthLevel(depthLevel);
		comment.setCreatedAt(LocalDateTime.now());

		return commentRepository.save(comment);
	}

	@Override
	public Like likePost(Long postId, Long userId) {

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

		Like existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

		if (existingLike != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has already liked this post");
		}

		Like like = new Like();
		like.setPostId(postId);
		like.setUserId(userId);
		like.setCreatedAt(LocalDateTime.now());

		Like savedLike = likeRepository.save(like);

		// update virality
		redisGuardService.incrementViralityScore(postId, HUMAN_LIKE_SCORE);

		Long postAuthorId = post.getAuthorId();

		if (isHumanUser(postAuthorId)) {
			sendNotificationForUserInteraction(postId, postAuthorId, userId, "liked your post");
		}

		return savedLike;
	}

	private void sendNotificationForBotInteraction(Long postId, Long userId, Long botId) {

		if (redisGuardService.hasRecentNotification(userId)) {
			redisGuardService.addPendingNotification(userId, "Bot " + botId + " replied to your post");
		} else {
			log.info("Push Notification -> User {}: Bot {} replied", userId, botId);
			redisGuardService.setNotificationThrottle(userId);
		}
	}

	private void sendNotificationForUserInteraction(Long postId, Long userId, Long actorUserId, String action) {

		if (redisGuardService.hasRecentNotification(userId)) {
			redisGuardService.addPendingNotification(userId, "User " + actorUserId + " " + action);
		} else {
			log.info("Push Notification -> User {}: User {} {}", userId, actorUserId, action);
			redisGuardService.setNotificationThrottle(userId);
		}
	}

	private boolean isHumanUser(Long userId) {
		return userRepository.existsById(userId);
	}
}
