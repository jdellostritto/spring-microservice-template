#!/bin/bash
# Comprehensive curl test suite for api-lifecycle-enterprise
# Tests all controllers and API versions

BASE_URL="http://localhost:8700"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}FlipFoundry API Test Suite${NC}"
echo -e "${BLUE}========================================${NC}\n"

# ========== GREETING CONTROLLER ==========
echo -e "${YELLOW}[GREETING CONTROLLER]${NC}"
echo "Base URL: $BASE_URL/flip/greeting/"

# V1 - Greet (Deprecated but still active)
echo -e "\n${GREEN}1. Greeting V1 - greet (DEPRECATED)${NC}"
echo "   Media Type: application/vnd.flipfoundry.greeting.v1+json"
echo "   Command:"
echo "   curl -X GET \"$BASE_URL/flip/greeting/greet?name=Alice\" \\"
echo "     -H \"Accept: application/vnd.flipfoundry.greeting.v1+json\""
curl -X GET "$BASE_URL/flip/greeting/greet?name=Alice" \
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json" \
  -w "\nStatus: %{http_code}\n\n"

# V1 - Greet with default name
echo -e "${GREEN}2. Greeting V1 - greet (default name)${NC}"
echo "   Command:"
echo "   curl -X GET \"$BASE_URL/flip/greeting/greet\" \\"
echo "     -H \"Accept: application/vnd.flipfoundry.greeting.v1+json\""
curl -X GET "$BASE_URL/flip/greeting/greet" \
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json" \
  -w "\nStatus: %{http_code}\n\n"

# V2 - Greet (Current version)
echo -e "${GREEN}3. Greeting V2 - greet (CURRENT)${NC}"
echo "   Media Type: application/vnd.flipfoundry.greeting.v2+json"
echo "   Command:"
echo "   curl -X GET \"$BASE_URL/flip/greeting/greet?name=Bob\" \\"
echo "     -H \"Accept: application/vnd.flipfoundry.greeting.v2+json\""
curl -X GET "$BASE_URL/flip/greeting/greet?name=Bob" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json" \
  -w "\nStatus: %{http_code}\n\n"

# V2 - Greet with special characters
echo -e "${GREEN}4. Greeting V2 - greet (special name)${NC}"
echo "   Command:"
echo "   curl -X GET \"$BASE_URL/flip/greeting/greet?name=JDello\" \\"
echo "     -H \"Accept: application/vnd.flipfoundry.greeting.v2+json\""
curl -X GET "$BASE_URL/flip/greeting/greet?name=JDello" \
  -H "Accept: application/vnd.flipfoundry.greeting.v2+json" \
  -w "\nStatus: %{http_code}\n\n"

# V1 - Depart (Deprecated, moved to DepartingController)
echo -e "${GREEN}5. Greeting V1 - depart (DEPRECATED - in GreetingController)${NC}"
echo "   Media Type: application/vnd.flipfoundry.greeting.v1+json"
echo "   Command:"
echo "   curl -X GET \"$BASE_URL/flip/greeting/depart\" \\"
echo "     -H \"Accept: application/vnd.flipfoundry.greeting.v1+json\""
curl -X GET "$BASE_URL/flip/greeting/depart" \
  -H "Accept: application/vnd.flipfoundry.greeting.v1+json" \
  -w "\nStatus: %{http_code}\n\n"

# ========== DEPART CONTROLLER ==========
echo -e "\n${YELLOW}[DEPARTING CONTROLLER]${NC}"
echo "Base URL: $BASE_URL/flip/departing/"

# V1 - Depart (Current location)
echo -e "${GREEN}6. Departing V1 - depart (CURRENT - in DepartingController)${NC}"
echo "   Media Type: application/vnd.flipfoundry.departing.v1+json"
echo "   Command:"
echo "   curl -X GET \"$BASE_URL/flip/departing/depart\" \\"
echo "     -H \"Accept: application/vnd.flipfoundry.departing.v1+json\""
curl -X GET "$BASE_URL/flip/departing/depart" \
  -H "Accept: application/vnd.flipfoundry.departing.v1+json" \
  -w "\nStatus: %{http_code}\n\n"

# ========== CONTENT NEGOTIATION TESTS ==========
echo -e "\n${YELLOW}[CONTENT NEGOTIATION TESTS]${NC}"

# Without Accept header (should use default)
echo -e "${GREEN}7. Greeting without Accept header (default)${NC}"
echo "   Command:"
echo "   curl -X GET \"$BASE_URL/flip/greeting/greet?name=Default\""
curl -X GET "$BASE_URL/flip/greeting/greet?name=Default" \
  -w "\nStatus: %{http_code}\n\n"

# Wrong media type (should fail or use default)
echo -e "${GREEN}8. Greeting with wrong Accept header${NC}"
echo "   Media Type: application/json (wrong)"
echo "   Command:"
echo "   curl -X GET \"$BASE_URL/flip/greeting/greet?name=Test\" \\"
echo "     -H \"Accept: application/json\""
curl -X GET "$BASE_URL/flip/greeting/greet?name=Test" \
  -H "Accept: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# ========== SUMMARY ==========
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}API Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "✓ GreetingController (/flip/greeting/):"
echo "  - GET /greet (V1 - DEPRECATED with counter)"
echo "  - GET /greet (V2 - CURRENT)"
echo "  - GET /depart (V1 - DEPRECATED, moved to DepartingController)"
echo -e "\n✓ DepartingController (/flip/departing/):"
echo "  - GET /depart (V1 - CURRENT with timestamp)"
echo -e "\n✓ Media Types Supported:"
echo "  - application/vnd.flipfoundry.greeting.v1+json"
echo "  - application/vnd.flipfoundry.greeting.v2+json"
echo "  - application/vnd.flipfoundry.departing.v1+json"
echo -e "\n${BLUE}========================================${NC}\n"
