package com.flipfoundry.tutorial.application.web.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingExceptionTest {

    @Test
    void messageConstructor_storesMessage() {
        var ex = new GreetingException("greeting failed");
        assertThat(ex.getMessage()).isEqualTo("greeting failed");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void causeConstructor_storesMessageAndCause() {
        var cause = new RuntimeException("root cause");
        var ex = new GreetingException("greeting failed", cause);
        assertThat(ex.getMessage()).isEqualTo("greeting failed");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
