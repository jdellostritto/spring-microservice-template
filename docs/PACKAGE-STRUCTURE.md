# Package Structure

This document describes the package hierarchy and architectural layers used in the spring-microservice-template.

## Overview

The project follows a **layered architecture** organized by functional domain rather than technical layer. This structure:

- Improves scalability as the codebase grows
- Isolates concerns by feature
- Makes dependencies explicit
- Facilitates testing and refactoring

## Package Hierarchy

```
com.flipfoundry.tutorial
├── application
│   ├── web
│   │   ├── controller          # HTTP request handlers
│   │   ├── dto                 # Data transfer objects (requests/responses)
│   │   ├── exception           # Web-specific exceptions
│   │   └── advice              # Global exception handling, interceptors
│   ├── service                 # Business logic and use cases
│   ├── repository              # Data access and persistence
│   ├── entity                  # JPA/database entities
│   ├── event                   # Domain events
│   ├── config                  # Spring configuration classes
│   └── filter                  # HTTP filters
└── Application                 # Main Spring Boot application class
```

## Detailed Layer Description

### 1. Web Layer (`web/`)

**Purpose:** HTTP interface and request/response handling

#### Controller (`web/controller`)

```java
package com.flipfoundry.tutorial.application.web.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flip/greeting")
public class GreetingController {
    
    private final GreetingService greetingService;
    
    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }
    
    @GetMapping("/greet")
    @RequestMapping(produces = "application/vnd.flipfoundry.greeting.v1+json")
    public GreetingDTO greet(@RequestParam(defaultValue = "World") String name) {
        String message = greetingService.generateGreeting(name);
        return new GreetingDTO(1, message);
    }
}
```

**Responsibilities:**
- Map HTTP requests to business operations
- Validate request parameters
- Handle content negotiation
- Return appropriate HTTP status codes

#### DTO (`web/dto`)

```java
package com.flipfoundry.tutorial.application.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GreetingDTO {
    private long version;
    private String message;
}
```

**Responsibilities:**
- Define API contract (request/response shapes)
- Perform input validation with Jakarta Bean Validation
- Separate API model from domain model
- Support versioning via multiple DTOs (GreetingDTOV2, etc.)

#### Exception Handling (`web/exception`)

```java
package com.flipfoundry.tutorial.application.web.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

#### Advice (`web/advice`)

```java
package com.flipfoundry.tutorial.application.web.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
```

### 2. Service Layer (`service/`)

**Purpose:** Business logic and orchestration

```java
package com.flipfoundry.tutorial.application.service;

import org.springframework.stereotype.Service;
import org.slf4j.MDC;

@Service
public class GreetingService {
    
    private final GreetingRepository greetingRepository;
    private final GreetingEventPublisher eventPublisher;
    
    public GreetingService(GreetingRepository greetingRepository, 
                          GreetingEventPublisher eventPublisher) {
        this.greetingRepository = greetingRepository;
        this.eventPublisher = eventPublisher;
    }
    
    public String generateGreeting(String name) {
        // Set correlation ID for distributed tracing
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlation-id", correlationId);
        
        try {
            String greeting = "Hello, " + name + "!";
            
            // Publish domain event
            eventPublisher.publishGreetingGenerated(name, greeting);
            
            return greeting;
        } finally {
            MDC.clear();
        }
    }
    
    public GreetingEntity saveGreeting(String name) {
        GreetingEntity entity = new GreetingEntity(name, generateGreeting(name));
        return greetingRepository.save(entity);
    }
}
```

**Responsibilities:**
- Implement business rules and workflows
- Coordinate between repositories and external services
- Manage transactions
- Publish domain events
- Handle correlation IDs for tracing

### 3. Repository Layer (`repository/`)

**Purpose:** Data access and persistence

```java
package com.flipfoundry.tutorial.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GreetingRepository extends JpaRepository<GreetingEntity, Long> {
    List<GreetingEntity> findByNameContainingIgnoreCase(String name);
}
```

**Responsibilities:**
- Abstract database operations
- Provide query methods
- Handle transaction boundaries
- Support Spring Data JPA

### 4. Entity Layer (`entity/`)

**Purpose:** JPA domain objects

```java
package com.flipfoundry.tutorial.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "greetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GreetingEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String message;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public GreetingEntity(String name, String message) {
        this.name = name;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
```

**Responsibilities:**
- Map to database tables
- Define ORM relationships
- Enforce database constraints

### 5. Event Layer (`event/`)

**Purpose:** Domain events for event-driven architecture

```java
package com.flipfoundry.tutorial.application.event;

import org.springframework.context.ApplicationEvent;

public class GreetingGeneratedEvent extends ApplicationEvent {
    
    private final String name;
    private final String greeting;
    
    public GreetingGeneratedEvent(Object source, String name, String greeting) {
        super(source);
        this.name = name;
        this.greeting = greeting;
    }
    
    public String getName() { return name; }
    public String getGreeting() { return greeting; }
}
```

**Publisher:**

```java
package com.flipfoundry.tutorial.application.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class GreetingEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public GreetingEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void publishGreetingGenerated(String name, String greeting) {
        GreetingGeneratedEvent event = new GreetingGeneratedEvent(
            this, name, greeting
        );
        eventPublisher.publishEvent(event);
    }
}
```

**Listener:**

```java
@Component
public class GreetingAuditListener {
    
    @EventListener
    public void onGreetingGenerated(GreetingGeneratedEvent event) {
        log.info("Greeting generated for {}: {}", event.getName(), event.getGreeting());
    }
}
```

### 6. Configuration Layer (`config/`)

**Purpose:** Spring configuration and setup

```java
package com.flipfoundry.tutorial.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/flip/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

### 7. Filter Layer (`filter/`)

**Purpose:** HTTP request/response intercepting

```java
package com.flipfoundry.tutorial.application.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.UUID;
import org.slf4j.MDC;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        MDC.put("correlation-id", correlationId);
        response.setHeader("X-Correlation-ID", correlationId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

## Scaling Patterns

### Vertical Scaling (Adding Features)

As you add new features, expand the package structure:

```
com.flipfoundry.tutorial
├── application
│   ├── greeting
│   │   ├── web/controller
│   │   ├── web/dto
│   │   ├── service
│   │   ├── repository
│   │   └── entity
│   ├── departing
│   │   ├── web/controller
│   │   ├── web/dto
│   │   ├── service
│   │   ├── repository
│   │   └── entity
│   ├── shared
│   │   ├── config
│   │   ├── filter
│   │   ├── web/advice
│   │   └── event
```

### Horizontal Scaling (Microservices)

When a feature grows into a separate service:

1. Extract the domain package (e.g., `greeting/`) into a new Maven module
2. Convert intra-service calls to REST/gRPC calls
3. Use event-driven communication for loosely-coupled updates

## Dependency Rules

### Allowed Dependencies

```
web/controller ──> service
service ──> repository, event, shared
repository ──> entity
entity ──> (no dependencies)
```

### Forbidden Dependencies

```
✗ service ──> web/controller  (reverse dependency)
✗ entity ──> service          (reverse dependency)
✗ repository ──> service      (reverse dependency)
```

### Enforcing Rules

Use ArchUnit for compile-time validation:

```java
@AnalyzeClasses(packages = "com.flipfoundry.tutorial")
public class PackageStructureTest {
    
    @ArchTest
    static ArchRule controllers_should_only_depend_on_services =
        classes()
            .that().resideInAPackage("..web.controller..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..service..", "..web.dto..", "..web.exception..");
}
```

## Testing Organization

Mirror package structure in test directory:

```
src/test/java/com/flipfoundry/tutorial/application
├── web
│   ├── controller
│   │   └── GreetingControllerTest.java
│   └── dto
│       └── GreetingDTOTest.java
├── service
│   └── GreetingServiceTest.java
└── repository
    └── GreetingRepositoryTest.java
```

## Cross-Cutting Concerns

Place shared concerns in `shared/` or `config/`:

- **Logging** - Configured in `logback-spring.xml` (see [LOGGING.md](LOGGING.md))
- **Monitoring** - Micrometer metrics in `config/`
- **Security** - Spring Security beans in `config/`
- **Transactions** - @Transactional at service layer
- **Validation** - Bean Validation (Jakarta) in DTOs

## Reference

- **Main Application Class** - `Application.java` (Spring Boot entry point)
- **API Versioning** - See [API-VERSIONING.md](API-VERSIONING.md) for media type patterns
- **URI Conventions** - See [URI-CONVENTIONS.md](URI-CONVENTIONS.md) for endpoint naming
- **Logging** - See [LOGGING.md](LOGGING.md) for correlation IDs and appender strategy
