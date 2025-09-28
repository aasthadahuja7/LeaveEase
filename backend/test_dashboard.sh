#!/bin/bash

echo "🎯 TESTING YOUR NEW DASHBOARD FEATURES"
echo "======================================"

echo ""
echo "📊 1. Dashboard Statistics:"
echo "-------------------------"
curl -s http://localhost:8080/api/dashboard/stats | jq .

echo ""
echo "📈 2. Quarterly Leave Data:"
echo "-------------------------"
curl -s http://localhost:8080/api/dashboard/quarterly-data | jq .

echo ""
echo "📅 3. Upcoming Leaves:"
echo "--------------------"
curl -s http://localhost:8080/api/dashboard/upcoming-leaves | jq .

echo ""
echo "👥 4. Team Members Currently on Leave:"
echo "------------------------------------"
curl -s http://localhost:8080/api/dashboard/team-on-leave | jq .

echo ""
echo "🔔 5. Recent Notifications:"
echo "-------------------------"
curl -s http://localhost:8080/api/dashboard/notifications | jq .

echo ""
echo "📋 6. All Leave Requests (with new fields):"
echo "------------------------------------------"
curl -s http://localhost:8080/leaves | jq '.[0:2]'  # Show first 2 records

echo ""
echo "🌐 7. Testing Dashboard HTML Access:"
echo "----------------------------------"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/dashboard.html)
if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Dashboard HTML is accessible at: http://localhost:8080/dashboard.html"
else
    echo "❌ Dashboard HTML returned HTTP code: $HTTP_CODE"
fi

echo ""
echo "🎉 DASHBOARD TEST COMPLETE!"
echo "=========================="
echo "Open your browser and visit: http://localhost:8080/dashboard.html"