package com.flipfoundry.tutorial.application.web.controller;

import com.flipfoundry.tutorial.application.dal.service.GreetingPersistenceService;
import com.flipfoundry.tutorial.application.events.GreetingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flipfoundry.tutorial.application.web.dto.DepartDTO;
import com.flipfoundry.tutorial.application.web.dto.GreetingDTO;
import com.flipfoundry.tutorial.application.web.dto.GreetingDTOV2;
import com.flipfoundry.tutorial.application.web.exception.GreetingException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>The greeting controller implements an endpoint that returns "Hello, `name`!"
 * to the caller. The class uses an atomic counter to track how many times the API
 * is called. The string and the counter are returned to the caller in a DTO.
 *
 * Updates 1.1 - Added a departing endpoint that returns a simple text message
 * Updates 1.2 - deprecated the depart endpoint moving it to a new departing controller.
 * Updates 1.3 - Overloaded the greet endpoint with a new one that returns a new representation.
 *              deprecated the V1 version of the greet endpoint and the counter associated with it.
 * Updates 1.4 - Added Hibernate persistence via GreetingPersistenceService.
 *              Added GET /history endpoint returning all persisted greetings.
 * </p>
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.4
 */
@RestController
@RequestMapping(value = "/flip/greeting/")
public class GreetingController {

    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

    private final GreetingEventPublisher eventPublisher;
    private final GreetingPersistenceService persistenceService;

    public GreetingController(GreetingEventPublisher eventPublisher,
                              GreetingPersistenceService persistenceService) {
        this.eventPublisher = eventPublisher;
        this.persistenceService = persistenceService;
    }

    private static String sanitize(String input) {
        return input == null ? null : input.replaceAll("[\r\n]", "");
    }

    private static final String TEMPLATE = "Hello, %s!";

    /**
     * An Atomic Long Counter used to track how many times the endpoint is called.
     * @deprecated As of release 1.3
     */
    @Deprecated(since = "1.3", forRemoval = false)
    private final AtomicLong counter = new AtomicLong();

    /**
     * <p>Greeting resource greet endpoint (V2). Saves the greeting and returns a DTO.</p>
     *
     * @param name An optional name parameter for the greeting
     * @return mono The greeting DTO V2
     * @see GreetingDTOV2
     * @since 1.3
     */
    @GetMapping(value = "/greet", produces = "application/vnd.flipfoundry.greeting.v2+json")
    public Mono<GreetingDTOV2> greetv2(@RequestParam(value = "name", defaultValue = "World") String name) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Greeting request received for name: {}", sanitize(name));
            }
            String greeting = String.format(TEMPLATE, name);

            // Persist — fire and forget on a blocking-safe scheduler
            Mono.fromCallable(() -> persistenceService.save(name, greeting))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            saved -> logger.debug("Greeting persisted id={}", saved.getId()),
                            err   -> logger.error("Failed to persist greeting for {}", sanitize(name), err)
                    );

            eventPublisher.publishGreeting(name, greeting, "en")
                    .subscribe(
                            meta -> logger.debug("GreetingEvent published for {}", sanitize(name)),
                            err  -> logger.error("Failed to publish GreetingEvent for {}", sanitize(name), err)
                    );

            return Mono.just(new GreetingDTOV2(greeting));
        } catch (Exception e) {
            throw new GreetingException("Failed to process greeting for: " + name, e);
        }
    }

    /**
     * <p>Returns all persisted greetings, newest first.</p>
     *
     * @return flux Stream of greeting DTOs from the database
     * @since 1.4
     */
    @GetMapping(value = "/history", produces = "application/vnd.flipfoundry.greeting.v2+json")
    public Flux<GreetingDTOV2> history() {
        return Mono.fromCallable(persistenceService::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapIterable(list -> list)
                .map(entity -> new GreetingDTOV2(entity.getContent()));
    }

    /**
     * <p>Greeting resource greet endpoint (V1 - Deprecated).</p>
     *
     * @param name An optional name parameter for the greeting
     * @return mono The greeting DTO with counter
     * @deprecated As of release 1.3, use {@link GreetingController#greetv2(String)} instead
     */
    @Deprecated(since = "1.3", forRemoval = false)
    @GetMapping(value = "/greet", produces = "application/vnd.flipfoundry.greeting.v1+json")
    public Mono<GreetingDTO> greet(@RequestParam(value = "name", defaultValue = "World") String name) {
        try {
            if (logger.isWarnEnabled()) {
                logger.warn("Deprecated v1 greeting endpoint called for name: {}", sanitize(name));
            }
            long count = counter.incrementAndGet();
            return Mono.just(new GreetingDTO(count, String.format(TEMPLATE, name)));
        } catch (Exception e) {
            throw new GreetingException("Failed to process v1 greeting for: " + name, e);
        }
    }

    /**
     * <p>Depart endpoint (Deprecated - moved to DepartingController).</p>
     *
     * @deprecated As of release 1.2, use {@link DepartingController#depart()} instead
     */
    @Deprecated(since = "1.2", forRemoval = true)
    @GetMapping(value = "/depart", produces = "application/vnd.flipfoundry.greeting.v1+json")
    public Mono<DepartDTO> depart() {
        try {
            logger.error("Deprecated depart endpoint called - will be removed in future version");
            return Mono.just(new DepartDTO("Goodbye"));
        } catch (Exception e) {
            throw new GreetingException("Failed to process depart request", e);
        }
    }
}
