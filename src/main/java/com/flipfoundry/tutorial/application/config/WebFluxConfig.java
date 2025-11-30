package com.flipfoundry.tutorial.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux Configuration.
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 * @since 2025-04-05
 */
@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

}
