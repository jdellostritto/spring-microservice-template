package com.flipfoundry.tutorial.application.dal.service;

import com.flipfoundry.tutorial.application.dal.entity.GreetingEntity;
import com.flipfoundry.tutorial.application.dal.repository.GreetingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class GreetingPersistenceService {

    private final GreetingRepository repository;

    public GreetingPersistenceService(GreetingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public GreetingEntity save(String name, String content) {
        GreetingEntity entity = GreetingEntity.builder()
                .name(name)
                .content(content)
                .build();
        GreetingEntity saved = repository.save(entity);
        log.debug("Saved greeting id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<GreetingEntity> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
