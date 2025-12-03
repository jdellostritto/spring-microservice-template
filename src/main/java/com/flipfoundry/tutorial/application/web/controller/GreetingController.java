package com.flipfoundry.tutorial.application.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flipfoundry.tutorial.application.web.dto.DepartDTO;
import com.flipfoundry.tutorial.application.web.dto.GreetingDTO;
import com.flipfoundry.tutorial.application.web.dto.GreetingDTOV2;

import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>The greeting controller implements an endpoint that returns "Hello, `name`!"
 * to the caller. The class uses an atomic counter to track how many times the API
 * is called. The string and the counter are returned to the caller in a DTO.
 *
 * Updates 1.1 - Added a departing endpoint that returns a simple text message
 * Updates 1.2 - deprecated the depart endpoint moving it to a new departing controller.
 * Updates 1.3 - Overloaded the greet endpoint with a new one that returns a new representation.
 *              deprecated the V1 version of the greet endpoint and the counter associated with it.
 * </p>
 *
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.3
 */
@RestController
@RequestMapping(value = "/flip/greeting/")
public class GreetingController {

    /**
     * A Simple template string that is formatted with
     * another string.
     */
    private static final String TEMPLATE = "Hello, %s!";

    /**
     * An Atomic Long Counter use to track how many times
     * the endpoint is called.
     * @deprecated As of release 1.3
     */
    @Deprecated(since = "1.3", forRemoval = false)
    private final AtomicLong counter = new AtomicLong();

    /**
     * <p>Greeting resource greet endpoint (V2). Scope is Get. Returns a simple greeting DTO.</p>
     *
     * @param name An optional name parameter for the greeting
     * @return mono The greeting DTO V2 containing the template string Hello, name
     * @see GreetingDTOV2
     * @since 1.3
     */
    @GetMapping(value = "/greet", produces="application/vnd.flipfoundry.greeting.v2+json")
    public Mono<GreetingDTOV2> greetv2(@RequestParam(value = "name", defaultValue = "World") String name) {
        return Mono.just( new GreetingDTOV2(String.format(TEMPLATE, name)));
    }


    /**
     * <p>Greeting resource greet endpoint (V1 - Deprecated). Scope is Get. Returns a greeting DTO with counter.</p>
     *
     * @param name An optional name parameter for the greeting
     * @return mono The greeting DTO containing a static long counter and the template string Hello, name
     * @see GreetingDTO
     * @deprecated As of release 1.3, use {@link GreetingController#greetv2(String)} instead
     * @since 1.0
     */
    @Deprecated(since = "1.3", forRemoval = false)
    @GetMapping(value = "/greet", produces="application/vnd.flipfoundry.greeting.v1+json")
    public Mono<GreetingDTO> greet(@RequestParam(value = "name", defaultValue = "World") String name) {
        return Mono.just( new GreetingDTO(counter.incrementAndGet(), String.format(TEMPLATE, name)));
    }

    /**
     * <p>Depart endpoint (Deprecated - moved to DepartingController).</p>
     *
     * @return mono The Depart DTO.
     * @see DepartDTO
     * @deprecated As of release 1.2, use {@link com.flipfoundry.tutorial.application.web.controller.DepartingController#depart()} instead
     * @since 1.1
     */
    @Deprecated(since = "1.2", forRemoval = true)
    @GetMapping(value = "/depart", produces="application/vnd.flipfoundry.greeting.v1+json")
    public Mono<DepartDTO> depart() {
        return Mono.just( new DepartDTO("Goodbye"));
    }
}
