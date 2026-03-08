# Endpoint Testing

> **Convention:** All API endpoints use content-negotiation via the `Accept` header. Always specify the versioned media type — do not rely on default content negotiation.

## Base URL

```
http://localhost:8700
```

Management (Actuator) endpoints are on a separate port:

```
http://localhost:9001
```

## Media Type Format

```
application/vnd.flipfoundry.{resource}.v{N}+json
```

---

## GreetingController — `/flip/greeting/`

### GET /flip/greeting/greet

**Query Parameters:** `name` (optional, defaults to `"World"`)

#### V1 — Deprecated

```bash
curl -X GET "http://localhost:8700/flip/greeting/greet?name=Alice" \
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json"
```

Response:

```json
{ "id": 1, "content": "Hello, Alice!" }
```

#### V2 — Current

```bash
curl -X GET "http://localhost:8700/flip/greeting/greet?name=Alice" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json"
```

Response:

```json
{ "content": "Hello, Alice!" }
```

V2 removes the `id` counter. See [API Versioning](./API-VERSIONING.md) and [Deprecation Strategy](./DEPRECATION.md).

#### Compare Versions

```bash
echo "=== V1 (Deprecated) ===" && \
curl -s "http://localhost:8700/flip/greeting/greet?name=Test" \
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json" | jq

echo "=== V2 (Current) ===" && \
curl -s "http://localhost:8700/flip/greeting/greet?name=Test" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json" | jq
```

---

## DepartingController — `/flip/departing/`

### GET /flip/departing/depart

```bash
curl -X GET "http://localhost:8700/flip/departing/depart" \
  -H "Accept: application/vnd.flipfoundry.departing.v1+json"
```

Response:

```json
{ "message": "Goodbye", "timestamp": "2026-01-15 14:30:45" }
```

---

## Actuator / Health

```bash
# Aggregate health
curl http://localhost:9001/actuator/health | jq

# Kubernetes probes
curl http://localhost:9001/actuator/health/liveness
curl http://localhost:9001/actuator/health/readiness

# Prometheus metrics
curl http://localhost:9001/actuator/prometheus
```

---

## Edge Cases

### No Accept Header

The server returns its lowest-priority registered media type. Always supply the `Accept` header explicitly.

```bash
curl "http://localhost:8700/flip/greeting/greet?name=Test"
```

### Unsupported Media Type → 406

```bash
curl "http://localhost:8700/flip/greeting/greet" \
  -H "Accept: application/json"
# → 406 Not Acceptable
```

### Pretty-Print with jq

```bash
curl -s "http://localhost:8700/flip/greeting/greet?name=Alice" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json" | jq
```

If you specify a media type that doesn't match any endpoint version:

```bash
curl -X GET "http://localhost:8080/api/greeting/greet" \
  -H "Accept: application/json"
```

The server may return:
- A default version
- An HTTP 406 (Not Acceptable) error
- The first available version

**Recommendation:** Always explicitly specify the correct media type.

### Accept Header with Quality Values

You can specify multiple acceptable formats with preference ordering:

```bash
curl -X GET "http://localhost:8080/api/greeting/greet?name=Multi" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json;q=1.0, application/vnd.flipfoundry.greeting.v1+json;q=0.9"
```

This requests V2 with highest priority (q=1.0), falling back to V1 (q=0.9).

---

## Testing Scripts

Two ready-to-run test scripts are provided:

### Bash Script: `test-api.sh`

Comprehensive test suite with colored output and all endpoint variations.

```bash
chmod +x test-api.sh
./test-api.sh
```

Runs 8 different API tests:
1. Greeting V1 with parameter
2. Greeting V1 with default name
3. Greeting V2 with parameter
4. Greeting V2 with special name
5. Greeting depart (deprecated)
6. Departing V1 (current)
7. Greeting without Accept header
8. Greeting with wrong Accept header

### PowerShell Script: `test-api.ps1`

Same comprehensive test suite for Windows environments.

```powershell
.\test-api.ps1
```

---

## Practical Examples

### Scenario 1: Client Migrating from V1 to V2

```bash
# Old client (V1)
curl -s "http://localhost:8080/api/greeting/greet?name=Legacy" \
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json" | jq

# New client (V2)
curl -s "http://localhost:8080/api/greeting/greet?name=Modern" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json" | jq

# Both endpoints coexist during transition
```

### Scenario 2: Integration Test

```bash
#!/bin/bash
# Test script for CI/CD pipeline

BASE_URL="http://localhost:8080"

# Test V2 endpoint
RESPONSE=$(curl -s "$BASE_URL/api/greeting/greet?name=TestUser" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json")

if echo "$RESPONSE" | grep -q "Hello, TestUser"; then
  echo "✓ Greeting V2 test passed"
else
  echo "✗ Greeting V2 test failed"
  exit 1
fi

# Test Departing endpoint
RESPONSE=$(curl -s "$BASE_URL/api/departing/depart" \
  -H "Accept: application/vnd.flipfoundry.departing.v1+json")

if echo "$RESPONSE" | grep -q "Goodbye"; then
  echo "✓ Departing test passed"
else
  echo "✗ Departing test failed"
  exit 1
fi
```

### Scenario 3: Request with Verbose Output

```bash
curl -v "http://localhost:8080/api/greeting/greet?name=Verbose" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json"
```

Shows:
- Request/response headers
- Media type negotiation details
- Timing information

---

## Response Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | OK - Request successful | Successful greeting endpoint |
| 404 | Not Found - Endpoint doesn't exist | Wrong path `/api/greeting/notfound` |
| 406 | Not Acceptable | Incompatible media type specified |
| 500 | Server Error | Application error |

---

## Troubleshooting

### Problem: Getting unexpected response format

**Solution:** Verify the `Accept` header exactly matches the media type:
- V1: `application/vnd.flipfoundry.greeting.v1+json`
- V2: `application/vnd.flipfoundry.greeting.v2+json`

Check with verbose output:
```bash
curl -v "http://localhost:8080/api/greeting/greet" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json"
```

### Problem: Cannot connect to server

**Solution:** Ensure the application is running:
```bash
# Check if port 8080 is listening
netstat -an | grep 8080

# Or on Windows PowerShell
Get-NetTCPConnection -LocalPort 8080 | Format-Table
```

### Problem: jq command not found

**Solution:** Install jq for JSON pretty-printing:

Ubuntu/Debian:
```bash
sudo apt-get install jq
```

macOS (with Homebrew):
```bash
brew install jq
```

Windows (with Chocolatey):
```powershell
choco install jq
```

Or use Python alternative:
```bash
curl -s "http://localhost:8080/api/greeting/greet" | python -m json.tool
```

---

## Summary

The api-lifecycle-enterprise API demonstrates modern API versioning patterns using content negotiation:

✓ **Multiple versions coexist** on same endpoint paths
✓ **Client control** via Accept header
✓ **Backward compatibility** maintained through V1 endpoints
✓ **Forward migration** supported via V2 endpoints
✓ **No URL versioning** needed (/v1/, /v2/)

This approach allows clients to upgrade at their own pace while the server maintains both API versions simultaneously.

