#!/bin/bash

# Abort on any error
set -e

# Store root directory for returning back
ROOT_DIR="$(pwd)"

# Handle exit
cleanup() {
    echo ""
    echo "⚠️  Stopping all processes..."

    # Kill Backend if running
    if [ -n "$BACKEND_PID" ] && kill -0 "$BACKEND_PID" 2>/dev/null; then
        echo "Stopping Backend (PID: $BACKEND_PID)..."
        kill "$BACKEND_PID" 2>/dev/null
    fi

    # Kill Frontend if running
    if [ -n "$FRONTEND_PID" ] && kill -0 "$FRONTEND_PID" 2>/dev/null; then
        echo "Stopping Frontend (PID: $FRONTEND_PID)..."
        kill "$FRONTEND_PID" 2>/dev/null
    fi

    echo "All processes stopped. Exiting."
    exit 0
}

# Trap common exit signals
trap cleanup SIGINT SIGTERM

echo "🚀 Starting SecureCourse..."

# ---------------- BACKEND ----------------
echo "➡️  Starting Backend..."
cd "$ROOT_DIR/backend"

if [ ! -x "./mvnw" ]; then
    echo "Making mvnw executable..."
    chmod +x mvnw
fi

./mvnw spring-boot:run &
BACKEND_PID=$!

cd "$ROOT_DIR"
echo "Backend PID: $BACKEND_PID"


# ---------------- FRONTEND ----------------
echo "➡️  Starting Frontend..."
cd "$ROOT_DIR/frontend"

npm run dev &
FRONTEND_PID=$!

cd "$ROOT_DIR"
echo "Frontend PID: $FRONTEND_PID"

echo ""
echo "✨ Application started successfully!"
echo "Press Ctrl+C to stop everything."
echo ""

# Wait until both exit
wait $BACKEND_PID $FRONTEND_PID
cleanup