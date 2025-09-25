@echo off
echo Java AI Playground Setup
echo ========================

REM Ask user to choose provider
echo Choose AI Provider:
echo 1) OpenAI
echo 2) Ollama
set /p choice="Enter choice (1-2): "

if "%choice%"=="1" (
    set AI_PROVIDER=openai
) else if "%choice%"=="2" (
    set AI_PROVIDER=ollama
) else (
    echo Invalid choice. Defaulting to OpenAI.
    set AI_PROVIDER=openai
)

REM Update .env file
if exist .env (
    powershell -Command "(Get-Content .env) -replace '^AI_PROVIDER=.*', 'AI_PROVIDER=%AI_PROVIDER%' | Set-Content .env"
) else (
    echo AI_PROVIDER=%AI_PROVIDER% > .env
    echo OPENAI_API_KEY=your-openai-api-key-here >> .env
    echo OLLAMA_BASE_URL=http://localhost:11434 >> .env
    echo OLLAMA_CHAT_MODEL=mistral >> .env
    echo OLLAMA_EMBEDDING_MODEL=nomic-embed-text >> .env
    echo SERVER_PORT=8080 >> .env
)

REM Load environment variables from .env file
for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
    if not "%%a"=="" if not "%%a:~0,1%"=="#" (
        set "%%a=%%b"
    )
)

echo Using AI Provider: %AI_PROVIDER%

if /i "%AI_PROVIDER%"=="openai" (
    REM OpenAI Configuration
    if "%OPENAI_API_KEY%"=="" (
        echo ERROR: OPENAI_API_KEY environment variable is not set!
        echo Please set your OpenAI API key in the .env file.
        pause
        exit /b 1
    )
    echo Using OpenAI with API key: %OPENAI_API_KEY:~0,8%...
) else if /i "%AI_PROVIDER%"=="ollama" (
    REM Ollama Configuration
    where ollama >nul 2>nul
    if %errorlevel% neq 0 (
        echo ERROR: Ollama not found. Please install Ollama first.
        pause
        exit /b 1
    )
    
    echo Checking if Ollama is running...
    curl -s %OLLAMA_BASE_URL%/api/tags >nul 2>nul
    if %errorlevel% neq 0 (
        echo Starting Ollama...
        start /b ollama serve
        timeout /t 5 /nobreak >nul
    ) else (
        echo Ollama is already running.
    )
    
    echo Checking and downloading required models...
    
    REM Check and pull chat model
    ollama list | findstr /C:"%OLLAMA_CHAT_MODEL%" >nul
    if %errorlevel% neq 0 (
        echo Downloading chat model: %OLLAMA_CHAT_MODEL%...
        ollama pull %OLLAMA_CHAT_MODEL%
    ) else (
        echo Chat model %OLLAMA_CHAT_MODEL% already available.
    )
    
    REM Check and pull embedding model
    ollama list | findstr /C:"%OLLAMA_EMBEDDING_MODEL%" >nul
    if %errorlevel% neq 0 (
        echo Downloading embedding model: %OLLAMA_EMBEDDING_MODEL%...
        ollama pull %OLLAMA_EMBEDDING_MODEL%
    ) else (
        echo Embedding model %OLLAMA_EMBEDDING_MODEL% already available.
    )
    
    echo Using Ollama with models: %OLLAMA_CHAT_MODEL%, %OLLAMA_EMBEDDING_MODEL%
) else (
    echo ERROR: Invalid AI_PROVIDER '%AI_PROVIDER%'. Must be 'openai' or 'ollama'.
    pause
    exit /b 1
)

REM Run the application
mvnw.cmd clean install && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=%AI_PROVIDER%