#!/bin/bash

echo "Testing Leave Management API..."

echo "1. Testing GET /leaves"
curl -s http://localhost:8080/leaves | jq . || echo "Failed to get leaves"

echo -e "\n2. Testing GET /api/dashboard/stats"
curl -s http://localhost:8080/api/dashboard/stats | jq . || echo "Failed to get dashboard stats"

echo -e "\n3. Testing static file access"
curl -I http://localhost:8080/dashboard.html

echo -e "\nAPI test completed."
