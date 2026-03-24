package com.flipfoundry.tutorial.application.dal.repository;

import com.flipfoundry.tutorial.application.dal.entity.GreetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GreetingRepository extends JpaRepository<GreetingEntity, UUID> {

    List<GreetingEntity> findAllByOrderByCreatedAtDesc();
}
