package com.social.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.api.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
