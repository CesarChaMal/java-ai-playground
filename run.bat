@echo off
echo Starting Java AI Playground...

REM Load environment variables from .env file
if exist .env (
    for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
)

REM Default to openai if AI_PROVIDER is not set
if "%AI_PROVIDER%"=="" set AI_PROVIDER=openai

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
    
    echo Ensuring required models are available...
    ollama pull %OLLAMA_CHAT_MODEL%
    ollama pull %OLLAMA_EMBEDDING_MODEL%
    echo Using Ollama with models: %OLLAMA_CHAT_MODEL%, %OLLAMA_EMBEDDING_MODEL%
) else (
    echo ERROR: Invalid AI_PROVIDER '%AI_PROVIDER%'. Must be 'openai' or 'ollama'.
    pause
    exit /b 1
)

REM Run the application
mvnw.cmd clean install && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=%AI_PROVIDER%