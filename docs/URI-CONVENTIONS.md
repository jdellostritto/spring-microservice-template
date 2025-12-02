# URI Conventions

This document describes the URI path conventions and RESTful best practices used in the spring-microservice-template.

## Overview

URIs follow a consistent pattern that emphasizes readability, predictability, and REST principles:

- Base path: `/flip/{resource}/`
- RESTful verbs via HTTP methods (GET, POST, PUT, DELETE)
- Resource-first naming
- Action-based endpoints only when necessary
- Plural resource names
- Hierarchical relationships

## Base URI Pattern

```
/flip/{resource}/{action}
```

### Components

| Component | Purpose | Example |
|-----------|---------|---------|
| `/flip/` | API namespace/context | `/flip/greeting/` |
| `{resource}` | The primary noun (plural) | `greeting`, `departing`, `user`, `order` |
| `/{action}` | Optional: specific action verb | `/greet`, `/farewell` (use sparingly) |

## RESTful Resource Endpoints

### Collection Operations

```
GET    /flip/greetings          - List all greetings
POST   /flip/greetings          - Create new greeting
```

### Single Resource Operations

```
GET    /flip/greetings/{id}     - Retrieve greeting by ID
PUT    /flip/greetings/{id}     - Update greeting (full replacement)
PATCH  /flip/greetings/{id}     - Update greeting (partial update)
DELETE /flip/greetings/{id}     - Delete greeting
```

## Action-Based Endpoints

Use action endpoints sparingly for operations that don't fit standard CRUD:

```
POST   /flip/greetings/{id}/greet    - Perform action on resource
POST   /flip/greetings/batch/process - Batch operation
```

### Examples of Valid Actions

- `/flip/greetings/batch/process` - Batch processing
- `/flip/orders/{id}/ship` - Complex state transition
- `/flip/users/{id}/verify-email` - Side-effect operation
- `/flip/resources/search` - Complex query operation

### Anti-Patterns (Avoid)

```
✗ /flip/greetings/list            (use GET /flip/greetings instead)
✗ /flip/greetings/get/{id}        (use GET /flip/greetings/{id} instead)
✗ /flip/createGreeting            (use POST /flip/greetings instead)
✗ /flip/deleteGreeting/{id}       (use DELETE /flip/greetings/{id} instead)
✗ /flip/updateGreeting/{id}       (use PUT /flip/greetings/{id} instead)
```

## Implementation Examples

### Standard CRUD Endpoints

#### Controller

```java
package com.flipfoundry.tutorial.application.web.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/flip/greetings")
public class GreetingController {
    
    private final GreetingService greetingService;
    
    // GET /flip/greetings - List all
    @GetMapping
    public Page<GreetingDTO> listGreetings(Pageable pageable) {
        return greetingService.findAll(pageable);
    }
    
    // POST /flip/greetings - Create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GreetingDTO createGreeting(@RequestBody CreateGreetingRequest request) {
        return greetingService.create(request);
    }
    
    // GET /flip/greetings/{id} - Retrieve
    @GetMapping("/{id}")
    public GreetingDTO getGreeting(@PathVariable Long id) {
        return greetingService.findById(id);
    }
    
    // PUT /flip/greetings/{id} - Update (full replacement)
    @PutMapping("/{id}")
    public GreetingDTO updateGreeting(
            @PathVariable Long id,
            @RequestBody UpdateGreetingRequest request) {
        return greetingService.update(id, request);
    }
    
    // PATCH /flip/greetings/{id} - Update (partial)
    @PatchMapping("/{id}")
    public GreetingDTO partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return greetingService.partialUpdate(id, updates);
    }
    
    // DELETE /flip/greetings/{id} - Delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGreeting(@PathVariable Long id) {
        greetingService.delete(id);
    }
}
```

### Action-Based Endpoints

```java
@RestController
@RequestMapping("/flip/greetings")
public class GreetingActionController {
    
    private final GreetingService greetingService;
    
    // POST /flip/greetings/{id}/greet - Custom action
    @PostMapping("/{id}/greet")
    public GreetingResponseDTO greet(
            @PathVariable Long id,
            @RequestBody GreetingRequest request) {
        return greetingService.greet(id, request);
    }
    
    // POST /flip/greetings/batch/process - Batch action
    @PostMapping("/batch/process")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BatchProcessResponse processBatch(
            @RequestBody BatchProcessRequest request) {
        return greetingService.processBatch(request);
    }
}
```

## Query Parameters

### Filtering

```
GET /flip/greetings?name=John        - Filter by name
GET /flip/greetings?status=active    - Filter by status
GET /flip/greetings?name=John&active=true  - Multiple filters
```

**Implementation:**

```java
@GetMapping
public Page<GreetingDTO> listGreetings(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Boolean active,
        Pageable pageable) {
    return greetingService.findAll(name, active, pageable);
}
```

### Pagination

```
GET /flip/greetings?page=0&size=20&sort=name,asc
```

**Spring Data Pageable:**

```java
@GetMapping
public Page<GreetingDTO> listGreetings(Pageable pageable) {
    // Automatically handles: page, size, sort
    return greetingService.findAll(pageable);
}
```

### Projection/Sparse Fields

```
GET /flip/greetings/{id}?fields=id,name,message
```

**Implementation:**

```java
@GetMapping("/{id}")
public GreetingDTO getGreeting(
        @PathVariable Long id,
        @RequestParam(required = false) List<String> fields) {
    GreetingDTO dto = greetingService.findById(id);
    // Filter fields if needed
    return dto;
}
```

## Relationship Navigation

### Nested Resources

```
GET    /flip/greetings/{id}/responses      - Retrieve related resources
POST   /flip/greetings/{id}/responses      - Create related resource
GET    /flip/greetings/{id}/responses/{rid} - Retrieve specific related resource
PUT    /flip/greetings/{id}/responses/{rid} - Update related resource
DELETE /flip/greetings/{id}/responses/{rid} - Delete related resource
```

**Implementation:**

```java
@RestController
@RequestMapping("/flip/greetings/{greetingId}/responses")
public class ResponseController {
    
    private final ResponseService responseService;
    
    @GetMapping
    public List<ResponseDTO> listResponses(@PathVariable Long greetingId) {
        return responseService.findByGreetingId(greetingId);
    }
    
    @GetMapping("/{responseId}")
    public ResponseDTO getResponse(
            @PathVariable Long greetingId,
            @PathVariable Long responseId) {
        return responseService.findById(greetingId, responseId);
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDTO createResponse(
            @PathVariable Long greetingId,
            @RequestBody CreateResponseRequest request) {
        return responseService.create(greetingId, request);
    }
}
```

## Versioning via Content Negotiation

URIs remain clean; versioning is handled via Accept headers:

```
GET /flip/greetings/{id}
Accept: application/vnd.flipfoundry.greeting.v1+json

GET /flip/greetings/{id}
Accept: application/vnd.flipfoundry.greeting.v2+json
```

See [API-VERSIONING.md](API-VERSIONING.md) for details.

## HTTP Status Codes

| Status | Meaning | Example Scenario |
|--------|---------|------------------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid input/request body |
| 401 | Unauthorized | Missing/invalid authentication |
| 403 | Forbidden | Authenticated but not authorized |
| 404 | Not Found | Resource doesn't exist |
| 406 | Not Acceptable | Unsupported media type/version |
| 409 | Conflict | Request conflicts with current state |
| 422 | Unprocessable Entity | Validation failed on request body |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Service temporarily down |

**Implementation:**

```java
@GetMapping("/{id}")
public ResponseEntity<GreetingDTO> getGreeting(@PathVariable Long id) {
    GreetingDTO dto = greetingService.findById(id);
    if (dto == null) {
        return ResponseEntity.notFound().build(); // 404
    }
    return ResponseEntity.ok(dto); // 200
}

@PostMapping
public ResponseEntity<GreetingDTO> createGreeting(
        @RequestBody CreateGreetingRequest request) {
    GreetingDTO dto = greetingService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto); // 201
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteGreeting(@PathVariable Long id) {
    greetingService.delete(id);
    return ResponseEntity.noContent().build(); // 204
}
```

## Error Response Format

Consistent error responses via global exception handler:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Greeting with ID 99 not found",
  "path": "/flip/greetings/99",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Implementation:**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .traceId(MDC.get("trace-id"))
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

## Best Practices

### 1. Use Nouns, Not Verbs

```
✓ GET  /flip/greetings/{id}
✗ GET  /flip/getGreeting/{id}

✓ POST /flip/greetings
✗ POST /flip/createGreeting
```

### 2. Use Plural Resources

```
✓ /flip/greetings
✗ /flip/greeting
```

### 3. Use HTTP Methods for Operations

```
✓ GET    /flip/greetings         (read)
✓ POST   /flip/greetings         (create)
✓ PUT    /flip/greetings/{id}    (update)
✓ DELETE /flip/greetings/{id}    (delete)

✗ GET /flip/greetings/read
✗ POST /flip/greetings/create
✗ POST /flip/greetings/update
✗ POST /flip/greetings/delete
```

### 4. Avoid Deep Nesting

```
✓ /flip/greetings/{id}/responses/{rid}      (2 levels)
✗ /flip/greetings/{id}/users/{uid}/responses/{rid}  (too deep)
```

### 5. Use Query Parameters for Filtering/Pagination

```
✓ GET /flip/greetings?name=John&page=0
✗ GET /flip/greetings/name/John/page/0
```

### 6. Keep URIs Lower Case

```
✓ /flip/greetings
✗ /flip/Greetings
```

### 7. Use Hyphens for Multi-word Resources

```
✓ /flip/greeting-responses
✗ /flip/greetingResponses
✗ /flip/greeting_responses
```

### 8. Version via Headers (Content-Negotiation)

```
✓ Accept: application/vnd.flipfoundry.greeting.v2+json
✗ GET /flip/v2/greetings
```

## Testing URIs

### Integration Testing

```java
@SpringBootTest
public class GreetingControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testListGreetings() throws Exception {
        mockMvc.perform(get("/flip/greetings")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());
    }
    
    @Test
    public void testCreateGreeting() throws Exception {
        mockMvc.perform(post("/flip/greetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Alice\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    public void testGetNotFound() throws Exception {
        mockMvc.perform(get("/flip/greetings/9999"))
            .andExpect(status().isNotFound());
    }
}
```

## Reference

- **API Versioning** - See [API-VERSIONING.md](API-VERSIONING.md) for content-negotiation details
- **Package Structure** - See [PACKAGE-STRUCTURE.md](PACKAGE-STRUCTURE.md) for controller organization
- **Logging** - See [LOGGING.md](LOGGING.md) for trace ID correlation across requests
- **REST Standards** - [RFC 7231](https://tools.ietf.org/html/rfc7231) HTTP Semantics
- **OpenAPI Spec** - Available at `http://localhost:8700/v3/api-docs.yaml`
