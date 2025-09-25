#!/bin/bash

echo "Java AI Playground Setup"
echo "========================"

# Ask user to choose provider
echo "Choose AI Provider:"
echo "1) OpenAI"
echo "2) Ollama"
read -p "Enter choice (1-2): " choice

case $choice in
    1)
        AI_PROVIDER="openai"
        ;;
    2)
        AI_PROVIDER="ollama"
        ;;
    *)
        echo "Invalid choice. Defaulting to OpenAI."
        AI_PROVIDER="openai"
        ;;
esac

# Update .env file
if [ -f .env ]; then
    sed -i.bak "s/^AI_PROVIDER=.*/AI_PROVIDER=$AI_PROVIDER/" .env
else
    echo "AI_PROVIDER=$AI_PROVIDER" > .env
    echo "OPENAI_API_KEY=your-openai-api-key-here" >> .env
    echo "OLLAMA_BASE_URL=http://localhost:11434" >> .env
    echo "OLLAMA_CHAT_MODEL=mistral" >> .env
    echo "OLLAMA_EMBEDDING_MODEL=nomic-embed-text" >> .env
    echo "SERVER_PORT=8080" >> .env
fi

# Load environment variables from .env file
export $(grep -v '^#' .env | xargs)

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
    
    echo "Checking and downloading required models..."
    
    # Check and pull chat model
    if ! ollama list | grep -q "${OLLAMA_CHAT_MODEL:-mistral}"; then
        echo "Downloading chat model: ${OLLAMA_CHAT_MODEL:-mistral}..."
        ollama pull ${OLLAMA_CHAT_MODEL:-mistral}
    else
        echo "Chat model ${OLLAMA_CHAT_MODEL:-mistral} already available."
    fi
    
    # Check and pull embedding model
    if ! ollama list | grep -q "${OLLAMA_EMBEDDING_MODEL:-nomic-embed-text}"; then
        echo "Downloading embedding model: ${OLLAMA_EMBEDDING_MODEL:-nomic-embed-text}..."
        ollama pull ${OLLAMA_EMBEDDING_MODEL:-nomic-embed-text}
    else
        echo "Embedding model ${OLLAMA_EMBEDDING_MODEL:-nomic-embed-text} already available."
    fi
    
    echo "Using Ollama with models: ${OLLAMA_CHAT_MODEL:-mistral}, ${OLLAMA_EMBEDDING_MODEL:-nomic-embed-text}"
    
else
    echo "ERROR: Invalid AI_PROVIDER '$AI_PROVIDER'. Must be 'openai' or 'ollama'."
    exit 1
fi

# Run the application
./mvnw clean install && ./mvnw spring-boot:run -Dspring-boot.run.profiles=$AI_PROVIDER