#!/bin/bash

# Abort on any error
set -e

# Save root directory so we always return
ROOT_DIR="$(pwd)"

# Cleanup function to stop both processes
cleanup() {
    echo ""
    echo "⚠️  Stopping all processes..."

    # Stop backend
    if [ -n "$BACKEND_PID" ] && kill -0 "$BACKEND_PID" 2>/dev/null; then
        echo "Stopping Backend (PID: $BACKEND_PID)..."
        kill "$BACKEND_PID" 2>/dev/null
    fi

    # Stop frontend
    if [ -n "$FRONTEND_PID" ] && kill -0 "$FRONTEND_PID" 2>/dev/null; then
        echo "Stopping Frontend (PID: $FRONTEND_PID)..."
        kill "$FRONTEND_PID" 2>/dev/null
    fi

    echo "All processes stopped. Exiting."
    exit 0
}

# Trap exit signals
trap cleanup SIGINT SIGTERM

echo "🚀 Starting SecureCourse..."

######################################
#            BACKEND START
######################################
echo "➡️  Starting Backend..."
cd "$ROOT_DIR/backend"

# Use mvnw if available, else use system mvn
if [ -f "./mvnw" ]; then
    echo "Using ./mvnw"
    chmod +x mvnw
    ./mvnw spring-boot:run &
else
    echo "Using system mvn"
    mvn spring-boot:run &
fi

BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

cd "$ROOT_DIR"

######################################
#            FRONTEND START
######################################
echo "➡️  Starting Frontend..."
cd "$ROOT_DIR/frontend"

# Install dependencies if node_modules missing
if [ ! -d "node_modules" ]; then
    echo "📦 node_modules not found. Running npm install..."
    npm install
fi

npm run dev &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"

cd "$ROOT_DIR"

######################################
#               DONE
######################################
echo ""
echo "✨ Application started successfully!"
echo "➡️  Backend running at: http://localhost:8080"
echo "➡️  Frontend running at: http://localhost:5173 (or printed by Vite)"
echo ""
echo "Press Ctrl+C to stop both."
echo ""

# Wait for both processes
wait $BACKEND_PID $FRONTEND_PID
cleanup
