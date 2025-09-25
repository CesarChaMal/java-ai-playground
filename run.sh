#!/bin/bash

echo "Starting Java AI Playground..."

# Load environment variables from .env file
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

# Default to openai if AI_PROVIDER is not set
AI_PROVIDER=${AI_PROVIDER:-openai}

echo "Using AI Provider: $AI_PROVIDER"

# Initialize SDKMAN if available
if [[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    sdk use java 21.fx-zulu 2>/dev/null || echo "Java 21 not available via SDKMAN, using system Java"
fi

if [ "$AI_PROVIDER" = "openai" ]; then
    # OpenAI Configuration
    if [ -z "$OPENAI_API_KEY" ]; then
        echo "ERROR: OPENAI_API_KEY environment variable is not set!"
        echo "Please set your OpenAI API key in the .env file."
        exit 1
    fi
    echo "Using OpenAI with API key: ${OPENAI_API_KEY:0:8}..."
    
elif [ "$AI_PROVIDER" = "ollama" ]; then
    # Ollama Configuration
    if ! command -v ollama &> /dev/null; then
        echo "ERROR: Ollama not found. Please install Ollama first."
        exit 1
    fi
    
    echo "Checking if Ollama is running..."
    if ! curl -s ${OLLAMA_BASE_URL:-http://localhost:11434}/api/tags > /dev/null 2>&1; then
        echo "Starting Ollama..."
        ollama serve > /dev/null 2>&1 &
        sleep 5
    else
        echo "Ollama is already running."
    fi
    
    echo "Ensuring required models are available..."
    ollama pull ${OLLAMA_CHAT_MODEL:-mistral}
    ollama pull ${OLLAMA_EMBEDDING_MODEL:-nomic-embed-text}
    echo "Using Ollama with models: ${OLLAMA_CHAT_MODEL:-mistral}, ${OLLAMA_EMBEDDING_MODEL:-nomic-embed-text}"
    
else
    echo "ERROR: Invalid AI_PROVIDER '$AI_PROVIDER'. Must be 'openai' or 'ollama'."
    exit 1
fi

# Run the application
./mvnw clean install && ./mvnw spring-boot:run -Dspring-boot.run.profiles=$AI_PROVIDER