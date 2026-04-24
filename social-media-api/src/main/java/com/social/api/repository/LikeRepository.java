package com.social.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.api.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {
	Like findByPostIdAndUserId(Long postId, Long userId);

}
