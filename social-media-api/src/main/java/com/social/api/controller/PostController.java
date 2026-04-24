package com.social.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.social.api.dto.ApiResponse;
import com.social.api.dto.CreateCommentRequest;
import com.social.api.dto.CreatePostRequest;
import com.social.api.dto.LikePostRequest;
import com.social.api.entity.Comment;
import com.social.api.entity.Like;
import com.social.api.entity.Post;
import com.social.api.service.PostService;

@RestController
public class PostController {

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<Post>> createPost(@RequestBody CreatePostRequest request) {
		try {
			Post post = postService.createPost(request.getAuthorId(), request.getContent());
			return ResponseEntity.ok(new ApiResponse<>(true, "Post Created sucessfully", post));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse<>(false, "Faild to Create post: " + e.getMessage(), null));
		}
	}

	@PostMapping("/{postId}/comment")
	public ResponseEntity<ApiResponse<Comment>> addComment(@PathVariable Long postId,
			@RequestBody CreateCommentRequest request) {
		try {
			Comment comment = postService.addComment(postId, request.getAuthorId(), request.getContent(),
					request.getBotId());
			return ResponseEntity.ok(new ApiResponse<>(true, "Comment added successfully", comment));
		} catch (Exception e) {
			if (e.getMessage().contains("429") || e.getMessage().contains("Too many Requests")) {
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
						.body(new ApiResponse<>(false, e.getMessage(), null));
			}

			if (e.getMessage().contains("Cooldown")) {
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
						.body(new ApiResponse<>(false, e.getMessage(), null));
			}

			if (e.getMessage().contains("not Found")) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), null));
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse<>(false, e.getMessage(), null));
		}
	}

	@PostMapping("/{postId}/like")
	public ResponseEntity<ApiResponse<Like>> likePost(@PathVariable Long postId, @RequestBody LikePostRequest request) {
		try {
			Like like = postService.likePost(postId, request.getUserId());
			return ResponseEntity.ok(new ApiResponse<>(true, "Post liked successfully", like));
		} catch (Exception e) {

			if (e.getMessage().contains("Not Found")) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), null));
			}

			if (e.getMessage().contains("already liked")) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ApiResponse<>(false, e.getMessage(), null));

			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse<>(false, e.getMessage(), null));

		}
	}

}
