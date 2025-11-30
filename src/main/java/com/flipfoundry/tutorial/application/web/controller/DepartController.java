package com.flipfoundry.tutorial.application.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flipfoundry.tutorial.application.web.dto.DepartDTO;

import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>The depart controller implements an endpoint that returns a "Goodbye" message along with a timestamp in the DTO</p>
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 */

@RestController
@RequestMapping(value = "/flip/departing/")
public class DepartController {

    /**
     * Default constructor for DepartController.
     */
    public DepartController() {
    }

    /**
     * <p>Depart endpoint. Returns departure information with timestamp.</p>
     *
     * @return mono The Depart DTO.
     * @see DepartDTO
     * @since 1.0
     */

    @GetMapping(value = "/depart", produces="application/vnd.flipfoundry.departing.v1+json")
    public Mono<DepartDTO> depart() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(timestamp.getTime());
        // S is the millisecond
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss:S");
        return Mono.just( new DepartDTO("Goodbye", simpleDateFormat.format(date)));
    }

}
