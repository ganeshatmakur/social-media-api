package com.social.api.service;

import com.social.api.entity.Comment;
import com.social.api.entity.Like;
import com.social.api.entity.Post;

public interface PostService {
	
	public Post createPost(Long authorId, String content);
	
	public Comment addComment(Long postId, Long authorId, String content,Long botId);
	
	public Like likePost(Long postId, Long userId);
	
 	

}
