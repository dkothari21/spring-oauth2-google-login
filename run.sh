#!/bin/bash

# Google OAuth2 Spring Boot Application - Run Script
# This script automatically loads credentials from .env file

echo "🚀 Google OAuth2 Spring Boot Application"
echo "=========================================="
echo ""

# Load environment variables from .env file if it exists
if [ -f ".env" ]; then
    echo "📋 Loading credentials from .env file..."
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ Credentials loaded"
    echo ""
else
    echo "⚠️  No .env file found"
    echo ""
    echo "Please create a .env file with your credentials:"
    echo ""
    echo "  cp .env.example .env"
    echo "  # Edit .env with your actual credentials"
    echo ""
fi

# Check if environment variables are set
if [ -z "$GOOGLE_CLIENT_ID" ]; then
    echo "❌ GOOGLE_CLIENT_ID is not set!"
    echo ""
    echo "Please set your Google OAuth2 credentials in .env file"
    echo ""
    exit 1
fi

if [ -z "$GOOGLE_CLIENT_SECRET" ]; then
    echo "❌ GOOGLE_CLIENT_SECRET is not set!"
    echo ""
    echo "Please set your Google OAuth2 client secret in .env file"
    echo ""
    exit 1
fi

echo "✅ Environment variables are set"
echo ""
echo "Client ID: ${GOOGLE_CLIENT_ID:0:20}..."
echo ""

# Check if port 8080 is available
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Port 8080 is already in use!"
    echo ""
    echo "Please stop the process using port 8080 or change the port in application.yml"
    echo ""
    exit 1
fi

echo "✅ Port 8080 is available"
echo ""

# Build and run
echo "🔨 Building application..."
./gradlew build -x test

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "🚀 Starting application..."
    echo ""
    echo "📝 Once started, authenticate at:"
    echo "   http://localhost:8080/oauth2/authorization/google"
    echo ""
    echo "📝 Then test with Swagger UI at:"
    echo "   http://localhost:8080/swagger-ui/index.html"
    echo ""
    echo "Press Ctrl+C to stop the application"
    echo ""
    echo "=========================================="
    echo ""
    
    ./gradlew bootRun
else
    echo ""
    echo "❌ Build failed! Please check the error messages above."
    exit 1
fi
