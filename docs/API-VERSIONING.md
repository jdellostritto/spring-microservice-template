# API Versioning

This project implements API versioning using content-negotiation with custom media types.

## Overview

Versioning is implemented via **content-negotiation** using Accept headers rather than URL-based versioning, allowing:

- Clean URIs without version numbers
- Multiple API versions running simultaneously
- Graceful deprecation and migration paths
- Clear API evolution tracking

## Versioning Strategy

### Media Type Format

```text
Accept: application/vnd.flipfoundry.{resource}.v{version}+json
```

### Examples

- `application/vnd.flipfoundry.greeting.v1+json` - Greeting API version 1
- `application/vnd.flipfoundry.greeting.v2+json` - Greeting API version 2
- `application/vnd.flipfoundry.departing.v1+json` - Departing API version 1

## Implementation

### Controller Configuration

```java
@RestController
@RequestMapping("/flip/greeting")
public class GreetingController {
    
    @GetMapping("/greet")
    @RequestMapping(produces = "application/vnd.flipfoundry.greeting.v1+json")
    public GreetingDTO greetV1(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new GreetingDTO(1, "Hello, " + name + "!");
    }
    
    @GetMapping("/greet")
    @RequestMapping(produces = "application/vnd.flipfoundry.greeting.v2+json")
    public GreetingDTOV2 greetV2(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new GreetingDTOV2(2, "Greetings, " + name + "!", System.currentTimeMillis());
    }
}
```

### Client Usage

#### cURL

```bash
# Request version 1
curl -H "Accept: application/vnd.flipfoundry.greeting.v1+json" \
  http://localhost:8700/flip/greeting/greet?name=Alice

# Request version 2
curl -H "Accept: application/vnd.flipfoundry.greeting.v2+json" \
  http://localhost:8700/flip/greeting/greet?name=Alice
```

#### Postman

1. Set header: `Accept: application/vnd.flipfoundry.greeting.v2+json`
2. Send GET request to: `http://localhost:8700/flip/greeting/greet`

#### JavaScript/Fetch

```javascript
fetch('http://localhost:8700/flip/greeting/greet', {
    headers: {
        'Accept': 'application/vnd.flipfoundry.greeting.v2+json'
    }
})
.then(response => response.json())
.then(data => console.log(data));
```

## Deprecation Approach

### Java Annotations

```java
@Deprecated(since = "0.0.2", forRemoval = true)
@RequestMapping(produces = "application/vnd.flipfoundry.greeting.v1+json")
public GreetingDTO greetV1(@RequestParam String name) {
    // Old implementation
}
```

### JavaDoc Documentation

```java
/**
 * @deprecated Since 0.0.2. Use {@link #greetV2(String)} instead.
 *             This endpoint will be removed in version 1.0.0.
 *             Please migrate to the v2 API.
 * 
 * @param name The person to greet
 * @return Legacy greeting response
 */
@Deprecated(since = "0.0.2", forRemoval = true)
public GreetingDTO greetV1(String name) { ... }
```

### Error Handling

When an unsupported media type is requested:

```
HTTP/1.1 406 Not Acceptable
Content-Type: application/json

{
  "error": "Not Acceptable",
  "message": "Media type 'application/vnd.flipfoundry.greeting.v3+json' is not supported"
}
```

## DTOs and Versioning

### Version 1 DTO

```java
@Data
@AllArgsConstructor
public class GreetingDTO {
    private long id;
    private String message;
}
```

### Version 2 DTO (Extended)

```java
@Data
@AllArgsConstructor
public class GreetingDTOV2 {
    private long id;
    private String message;
    private long timestamp;  // New field
}
```

## Migration Guide for Clients

### Step 1: Detect Current Version

Check your client's Accept header usage:

```bash
# Current (v1)
curl -H "Accept: application/vnd.flipfoundry.greeting.v1+json" http://localhost:8700/flip/greeting/greet

# Migrate to (v2)
curl -H "Accept: application/vnd.flipfoundry.greeting.v2+json" http://localhost:8700/flip/greeting/greet
```

### Step 2: Update Request Headers

Change your API calls to use the new media type.

### Step 3: Handle New Fields

Add handling for new fields in the response (e.g., `timestamp` in v2).

### Step 4: Test Thoroughly

Ensure your application works with the new response format.

## Best Practices

1. **Maintain compatibility** - Don't remove fields from old versions; add new fields to new versions
2. **Clear deprecation timeline** - Always provide a sunset date (at least 6 months)
3. **Document changes** - Clearly document what changed between versions
4. **Test both versions** - Maintain integration tests for multiple API versions
5. **Version incrementally** - Only increment when API contracts change significantly
6. **Monitor usage** - Track which clients are using which versions

## OpenAPI Documentation

The OpenAPI specification includes version information:

```yaml
info:
  title: Flip Foundry Tutorial - Greeting and Departing Resource
  version: 0.0.1-SNAPSHOT

paths:
  /flip/greeting/greet:
    get:
      responses:
        '200':
          content:
            application/vnd.flipfoundry.greeting.v1+json:
              schema: { ... }
            application/vnd.flipfoundry.greeting.v2+json:
              schema: { ... }
```

Access at: `http://localhost:8700/test/index.html`
