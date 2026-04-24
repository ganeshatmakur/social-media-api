package com.social.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.api.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
