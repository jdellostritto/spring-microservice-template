# Deprecation Strategy

This document describes how to deprecate APIs and manage versioning transitions while maintaining backward compatibility.

## Overview

The deprecation strategy enables:

- Smooth migration paths for clients
- Clear communication of changes
- Extended support periods
- Zero-downtime API evolution

## Deprecation Lifecycle

```
┌─────────────┐
│  New API    │ Version 1.0 published
└──────┬──────┘
       │
       ├─────────────────────► New features added (v1.1, v1.2, ...)
       │
       ├─────────────────────► Design flaw discovered or better design available
       │
       ├─────────────────────► Mark as @Deprecated with sunset date
       │                       (6 months minimum notice)
       │
       ├─────────────────────► Deprecation period (minimum 6 months)
       │                       Old API still functional
       │                       Warnings in logs, documentation, OpenAPI
       │
       ├─────────────────────► Sunset date announced
       │                       2-3 months before removal
       │
       └─────────────────────► API removed
                              forRemoval=true
                              404 responses returned
```

## Marking APIs as Deprecated

### Java @Deprecated Annotation

```java
package com.flipfoundry.tutorial.application.web.controller;

@RestController
@RequestMapping("/flip/greetings")
public class GreetingController {
    
    /**
     * Retrieve a greeting by ID.
     * 
     * @deprecated Since 0.0.2. Use {@link #getGreetingV2(Long)} instead.
     *             This endpoint will be removed on 2025-07-01.
     *             Please migrate to the v2 API:
     *             Accept: application/vnd.flipfoundry.greeting.v2+json
     * 
     * @param id The greeting ID
     * @return The greeting object
     */
    @Deprecated(since = "0.0.2", forRemoval = false)
    @GetMapping("/{id}")
    @RequestMapping(produces = "application/vnd.flipfoundry.greeting.v1+json")
    public GreetingDTOV1 getGreetingV1(@PathVariable Long id) {
        log.warn("Deprecated API called: GET /flip/greetings/{id} with v1 media type");
        return greetingService.findByIdV1(id);
    }
    
    @GetMapping("/{id}")
    @RequestMapping(produces = "application/vnd.flipfoundry.greeting.v2+json")
    public GreetingDTOV2 getGreetingV2(@PathVariable Long id) {
        return greetingService.findByIdV2(id);
    }
}
```

### @Deprecated Attributes

| Attribute | Purpose | Example |
|-----------|---------|---------|
| `since` | Version when deprecated | `since = "0.0.2"` |
| `forRemoval` | Will be removed | `forRemoval = false` (initial), `forRemoval = true` (2+ versions later) |

### Timeline Example

**Version 0.0.2 (Initial Deprecation)**
```java
@Deprecated(since = "0.0.2", forRemoval = false)
```
- Message: "Deprecated since 0.0.2. Scheduled for removal in version 1.0.0 (2025-07-01)"

**Version 0.1.0 (6 months later)**
```java
@Deprecated(since = "0.0.2", forRemoval = true)
```
- Message: "Will be removed in next major release"

**Version 1.0.0 (1+ year later)**
```java
// Remove completely
```
- API no longer exists, returns 404

## Migration Guidance in JavaDoc

### Comprehensive Migration Example

```java
/**
 * Retrieve a greeting by ID (old v1 API).
 * 
 * <p><strong>Deprecation Notice:</strong></p>
 * <ul>
 *   <li>Deprecated since: 0.0.2 (released 2024-07-01)</li>
 *   <li>Scheduled removal: 1.0.0 (planned 2025-07-01)</li>
 *   <li>Reason: Enhanced response format with timestamp and metadata</li>
 * </ul>
 * 
 * <p><strong>Migration Path:</strong></p>
 * <ol>
 *   <li>Update your Accept header from:
 *       <pre>Accept: application/vnd.flipfoundry.greeting.v1+json</pre>
 *       to:
 *       <pre>Accept: application/vnd.flipfoundry.greeting.v2+json</pre>
 *   </li>
 *   <li>Update your code to handle the new response format:
 *       <pre>{@code
 * // Old v1 format:
 * {
 *   "id": 123,
 *   "message": "Hello Alice"
 * }
 * 
 * // New v2 format:
 * {
 *   "id": 123,
 *   "message": "Hello Alice",
 *   "timestamp": 1640000000000,
 *   "metadata": {
 *     "version": 2,
 *     "locale": "en-US"
 *   }
 * }
 * }</pre>
 *   </li>
 *   <li>Test thoroughly in development before deploying to production</li>
 * </ol>
 * 
 * <p><strong>Support Timeline:</strong></p>
 * <ul>
 *   <li>Until 2025-04-01: Full support for v1 API</li>
 *   <li>2025-04-01 to 2025-07-01: 3-month final warning period</li>
 *   <li>After 2025-07-01: API returns 406 Not Acceptable</li>
 * </ul>
 * 
 * @deprecated Use {@link #getGreetingV2(Long)} instead
 * @param id The greeting ID
 * @return The greeting object (v1 format)
 * @throws ResourceNotFoundException if greeting not found
 * @see #getGreetingV2(Long) for the replacement v2 API
 */
@Deprecated(since = "0.0.2", forRemoval = false)
public GreetingDTOV1 getGreetingV1(@PathVariable Long id) {
    return greetingService.findByIdV1(id);
}
```

## Deprecation Warnings in Application

### Logging Deprecation Calls

```java
@Component
public class DeprecationWarningFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(DeprecationWarningFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {
        
        String contentType = request.getHeader("Accept");
        
        if (isDeprecatedMediaType(contentType)) {
            String deprecationMessage = getDeprecationMessage(contentType);
            log.warn("DEPRECATED API CALL: {} {} - {}",
                request.getMethod(),
                request.getRequestURI(),
                deprecationMessage,
                new DeprecationWarning(contentType));
            
            response.addHeader("Deprecation", "true");
            response.addHeader("Warning", "299 - \"Deprecated API\": " + deprecationMessage);
            response.addHeader("Sunset", "Sun, 01 Jul 2025 00:00:00 GMT");
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isDeprecatedMediaType(String contentType) {
        return contentType != null && 
               (contentType.contains("v1+json") || 
                contentType.contains("v0+json"));
    }
    
    private String getDeprecationMessage(String contentType) {
        if (contentType.contains("v1+json")) {
            return "API v1 deprecated since 0.0.2. Migrate to v2. " +
                   "Sunset: 2025-07-01. See https://docs/API-VERSIONING.md";
        }
        return "This API version is deprecated. Please migrate to the latest version.";
    }
}
```

### HTTP Deprecation Headers

The filter adds standard HTTP headers:

```
Deprecation: true
Warning: 299 - "Deprecated API": API v1 deprecated since 0.0.2. Migrate to v2.
Sunset: Sun, 01 Jul 2025 00:00:00 GMT
```

**Client-side handling:**

```javascript
fetch('http://localhost:8700/flip/greetings/123', {
    headers: {
        'Accept': 'application/vnd.flipfoundry.greeting.v1+json'
    }
})
.then(response => {
    // Check for deprecation headers
    if (response.headers.get('Deprecation') === 'true') {
        console.warn('Warning: ' + response.headers.get('Warning'));
        console.warn('Sunset: ' + response.headers.get('Sunset'));
    }
    return response.json();
})
.then(data => console.log(data));
```

## OpenAPI Specification

### Deprecation in OpenAPI

```yaml
openapi: 3.0.0
info:
  title: Flip Foundry Tutorial API
  version: 0.0.2

paths:
  /flip/greetings/{id}:
    get:
      summary: Retrieve a greeting by ID
      deprecated: true  # Mark entire endpoint as deprecated
      parameters:
        - name: Accept
          in: header
          schema:
            type: string
            default: "application/vnd.flipfoundry.greeting.v2+json"
      responses:
        '200':
          description: Success
          content:
            application/vnd.flipfoundry.greeting.v1+json:
              schema:
                type: object
                description: "DEPRECATED - Use v2 instead"
                properties:
                  id:
                    type: integer
                  message:
                    type: string
            application/vnd.flipfoundry.greeting.v2+json:
              schema:
                type: object
                description: "RECOMMENDED - Latest version"
                properties:
                  id:
                    type: integer
                  message:
                    type: string
                  timestamp:
                    type: integer
        '406':
          description: Not Acceptable - Unsupported media type
```

### OpenAPI Generation

The Springdoc plugin automatically detects `@Deprecated`:

```bash
make openapi
```

Generated `build/openapi/openapi.yaml` includes:

```yaml
- description: "DEPRECATED (since 0.0.2)"
  deprecated: true
```

## Handling 406 Not Acceptable

### After Sunset Date

When deprecation period ends, return 406:

```java
@Component
public class DeprecationEnforcementFilter extends OncePerRequestFilter {
    
    // Configuration: Load from application-{profile}.yml
    @Value("${api.v1.sunset-date}")
    private LocalDateTime v1SunsetDate;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {
        
        String contentType = request.getHeader("Accept");
        
        // After sunset, enforce removal
        if (LocalDateTime.now().isAfter(v1SunsetDate) &&
            isRemovedMediaType(contentType)) {
            
            response.sendError(HttpStatus.NOT_ACCEPTABLE.value(),
                "API v1 sunset date (2025-07-01) has passed. " +
                "Only v2 supported. Accept: application/vnd.flipfoundry.greeting.v2+json");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### Error Response (406 Not Acceptable)

```json
{
  "timestamp": "2025-07-02T10:30:00Z",
  "status": 406,
  "error": "Not Acceptable",
  "message": "API v1 sunset date (2025-07-01) has passed. Only v2 supported. Accept: application/vnd.flipfoundry.greeting.v2+json",
  "path": "/flip/greetings/123",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## Deprecation in Documentation

### README Update

When deprecating, update README with deprecation notice:

```markdown
## API Versions

### Current (Recommended)
- **v2** - Latest and recommended (Accept: `application/vnd.flipfoundry.greeting.v2+json`)
- Available since: 0.0.2
- Status: Active

### Deprecated
- **v1** - Deprecated since 0.0.2
- Sunset date: 2025-07-01
- Migration guide: See [API-VERSIONING.md](docs/API-VERSIONING.md)
```

### Changelog Entry

```markdown
## Version 0.0.2 (2024-07-01)

### Deprecations
- Deprecated Greeting API v1 (media type: `application/vnd.flipfoundry.greeting.v1+json`)
  - Scheduled removal: Version 1.0.0 (2025-07-01)
  - Reason: Enhanced response format with timestamp and metadata
  - Migration: Update Accept header to `application/vnd.flipfoundry.greeting.v2+json`
  - See: [API-VERSIONING.md](docs/API-VERSIONING.md) for migration details

### New Features
- Added Greeting API v2 with enhanced metadata
```

## Testing Deprecations

### Deprecation Detection Tests

```java
@SpringBootTest
public class DeprecationTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testV1DeprecatedHeaderPresent() throws Exception {
        mockMvc.perform(get("/flip/greetings/1")
                .header("Accept", "application/vnd.flipfoundry.greeting.v1+json"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Deprecation"))
            .andExpect(header().string("Deprecation", "true"))
            .andExpect(header().exists("Sunset"));
    }
    
    @Test
    public void testV2NoDeprecationHeader() throws Exception {
        mockMvc.perform(get("/flip/greetings/1")
                .header("Accept", "application/vnd.flipfoundry.greeting.v2+json"))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist("Deprecation"));
    }
    
    @Test
    public void testV1ReturnsNotAcceptableAfterSunset() throws Exception {
        // Requires mocking system clock or configuration
        mockMvc.perform(get("/flip/greetings/1")
                .header("Accept", "application/vnd.flipfoundry.greeting.v1+json"))
            .andExpect(status().isNotAcceptable());
    }
}
```

## Deprecation Timeline Template

```
Deprecation Announced: [Release] (MM-DD-YYYY)
Deprecation Period: [Minimum 6 months]
Support Until: [MM-DD-YYYY]
Final Warning Period: [3 months before sunset]
Final Warning Until: [MM-DD-YYYY]
Sunset Date: [MM-DD-YYYY]
Removal: [Next Major Release]

Affected Resources:
- API v1 (media type: application/vnd.flipfoundry.greeting.v1+json)

Migration Instructions:
1. Update Accept header to: application/vnd.flipfoundry.greeting.v2+json
2. Review [API-VERSIONING.md](docs/API-VERSIONING.md) for response format changes
3. Test in development environment
4. Deploy to production before [MM-DD-YYYY]

Contact: [Support Channel]
```

## Reference

- **API Versioning** - See [API-VERSIONING.md](API-VERSIONING.md) for content-negotiation patterns
- **HTTP Semantics** - [RFC 7231](https://tools.ietf.org/html/rfc7231) Status Code Definitions
- **OpenAPI** - Available at `http://localhost:8700/v3/api-docs.yaml`
- **Java @Deprecated** - [Oracle Documentation](https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html)
- **RFC 8594** - The Sunset HTTP Header Field
