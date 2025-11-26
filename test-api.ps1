# Comprehensive curl test suite for api-lifecycle-enterprise
# Tests all controllers and API versions (PowerShell version)

$BaseUrl = "http://localhost:8080"

# Colors
function Write-Title { Write-Host $args[0] -ForegroundColor Blue }
function Write-Section { Write-Host "`n$($args[0])" -ForegroundColor Yellow }
function Write-Test { Write-Host "`n$($args[0])" -ForegroundColor Green }

Write-Title "========================================"
Write-Title "api-lifecycle-enterprise API Test Suite"
Write-Title "========================================"

# ========== GREETING CONTROLLER ==========
Write-Section "[GREETING CONTROLLER]"
Write-Host "Base URL: $BaseUrl/api/greeting/"

# V1 - Greet (Deprecated)
Write-Test "1. Greeting V1 - greet (DEPRECATED)"
Write-Host "   Media Type: application/vnd.flipfoundry.greeting.v1+json"
Write-Host "   Command:"
Write-Host "   curl -X GET `"$BaseUrl/api/greeting/greet?name=Alice`" ``"
Write-Host "     -H `"Accept: application/vnd.flipfoundry.greeting.v1+json`""
$response = curl.exe -X GET "$BaseUrl/api/greeting/greet?name=Alice" `
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json"
Write-Host $response
Write-Host ""

# V1 - Greet with default name
Write-Test "2. Greeting V1 - greet (default name)"
Write-Host "   Command:"
Write-Host "   curl -X GET `"$BaseUrl/api/greeting/greet`" ``"
Write-Host "     -H `"Accept: application/vnd.flipfoundry.greeting.v1+json`""
$response = curl.exe -X GET "$BaseUrl/api/greeting/greet" `
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json"
Write-Host $response
Write-Host ""

# V2 - Greet (Current)
Write-Test "3. Greeting V2 - greet (CURRENT)"
Write-Host "   Media Type: application/vnd.flipfoundry.greeting.v2+json"
Write-Host "   Command:"
Write-Host "   curl -X GET `"$BaseUrl/api/greeting/greet?name=Bob`" ``"
Write-Host "     -H `"Accept: application/vnd.flipfoundry.greeting.v2+json`""
$response = curl.exe -X GET "$BaseUrl/api/greeting/greet?name=Bob" `
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json"
Write-Host $response
Write-Host ""

# V2 - Greet with special characters
Write-Test "4. Greeting V2 - greet (special name)"
Write-Host "   Command:"
Write-Host "   curl -X GET `"$BaseUrl/api/greeting/greet?name=JDello`" ``"
Write-Host "     -H `"Accept: application/vnd.flipfoundry.greeting.v2+json`""
$response = curl.exe -X GET "$BaseUrl/api/greeting/greet?name=JDello" `
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json"
Write-Host $response
Write-Host ""

# V1 - Depart (Deprecated, in GreetingController)
Write-Test "5. Greeting V1 - depart (DEPRECATED - in GreetingController)"
Write-Host "   Media Type: application/vnd.flipfoundry.greeting.v1+json"
Write-Host "   Command:"
Write-Host "   curl -X GET `"$BaseUrl/api/greeting/depart`" ``"
Write-Host "     -H `"Accept: application/vnd.flipfoundry.greeting.v1+json`""
$response = curl.exe -X GET "$BaseUrl/api/greeting/depart" `
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json"
Write-Host $response
Write-Host ""

# ========== DEPART CONTROLLER ==========
Write-Section "[DEPART CONTROLLER]"
Write-Host "Base URL: $BaseUrl/api/departing/"

# V1 - Depart (Current)
Write-Test "6. Departing V1 - depart (CURRENT - in DepartController)"
Write-Host "   Media Type: application/vnd.flipfoundry.departing.v1+json"
Write-Host "   Command:"
Write-Host "   curl -X GET `"$BaseUrl/api/departing/depart`" ``"
Write-Host "     -H `"Accept: application/vnd.flipfoundry.departing.v1+json`""
$response = curl.exe -X GET "$BaseUrl/api/departing/depart" `
  -H "Accept: application/vnd.flipfoundry.departing.v1+json"
Write-Host $response
Write-Host ""

# ========== CONTENT NEGOTIATION TESTS ==========
Write-Section "[CONTENT NEGOTIATION TESTS]"

# Without Accept header
Write-Test "7. Greeting without Accept header (default)"
Write-Host "   Command:"
Write-Host "   curl -X GET `"$BaseUrl/api/greeting/greet?name=Default`""
$response = curl.exe -X GET "$BaseUrl/api/greeting/greet?name=Default"
Write-Host $response
Write-Host ""

# Wrong media type
Write-Test "8. Greeting with wrong Accept header"
Write-Host "   Media Type: application/json (wrong)"
Write-Host "   Command:"
Write-Host "   curl -X GET `"$BaseUrl/api/greeting/greet?name=Test`" ``"
Write-Host "     -H `"Accept: application/json`""
$response = curl.exe -X GET "$BaseUrl/api/greeting/greet?name=Test" `
  -H "Accept: application/json"
Write-Host $response
Write-Host ""

# ========== SUMMARY ==========
Write-Title "`n========================================"
Write-Title "API Test Summary"
Write-Title "========================================"
Write-Host "✓ GreetingController:"
Write-Host "  - GET /api/greeting/greet (V1 - DEPRECATED)"
Write-Host "  - GET /api/greeting/greet (V2 - CURRENT)"
Write-Host "  - GET /api/greeting/depart (V1 - DEPRECATED)"
Write-Host ""
Write-Host "✓ DepartController:"
Write-Host "  - GET /api/departing/depart (V1 - CURRENT)"
Write-Host ""
Write-Host "✓ Media Types Supported:"
Write-Host "  - application/vnd.flipfoundry.greeting.v1+json"
Write-Host "  - application/vnd.flipfoundry.greeting.v2+json"
Write-Host "  - application/vnd.flipfoundry.departing.v1+json"
Write-Title "========================================"
Write-Host ""
