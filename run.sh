#!/bin/bash
# =======================================================
# Start DocPortal Local Server
# =======================================================

cd "$(dirname "$0")"

CLASSPATH="/Users/aniketkumar/.m2/repository/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar:sample/src/main/webapp/WEB-INF/lib/*:sample/build/classes"

echo "======================================================="
echo "🏥 Starting DocPortal Web Portal..."
echo "👉 Open in your browser: http://localhost:8080/Welcome.html"
echo "======================================================="

java -cp "$CLASSPATH" AppServer
