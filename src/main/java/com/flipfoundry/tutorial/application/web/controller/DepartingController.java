package com.flipfoundry.tutorial.application.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flipfoundry.tutorial.application.web.dto.DepartDTO;

import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>The departing controller implements an endpoint that returns a "Goodbye" message along with a timestamp in the DTO</p>
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 */

@RestController
@RequestMapping(value = "/flip/departing/")
public class DepartingController {

    private static final Logger logger = LoggerFactory.getLogger(DepartingController.class);

    /**
     * <p>Depart endpoint. Returns departure information with timestamp.</p>
     *
     * @return mono The Depart DTO.
     * @see DepartDTO
     * @since 1.0
     */

    @GetMapping(value = "/depart", produces="application/vnd.flipfoundry.departing.v1+json")
    public Mono<DepartDTO> depart() {
        logger.info("Departing request received");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(timestamp.getTime());
        // S is the millisecond
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss:S");
        String formattedTimestamp = simpleDateFormat.format(date);
        logger.debug("Departing at: {}", formattedTimestamp);
        return Mono.just( new DepartDTO("Goodbye", formattedTimestamp));
    }

}
