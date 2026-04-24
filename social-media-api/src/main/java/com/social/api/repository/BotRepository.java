package com.social.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.api.entity.Bot;

public interface BotRepository extends JpaRepository<Bot, Long> {

}
