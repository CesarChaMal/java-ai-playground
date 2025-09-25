package org.vaadin.marcus.langchain4j;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "langchain4j")
public class LangChain4jProperties {

    // Used for Ollama only (OpenAI uses its own endpoint)
    private String baseUrl = "http://localhost:11434";
    
    // Content retriever configuration
    private int maxResults = 2;
    private double minScore = 0.6;
    private int tokenLimit = 1000;

    private Ollama ollama = new Ollama();
    private OpenAi openAi = new OpenAi();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Ollama getOllama() {
        return ollama;
    }

    public void setOllama(Ollama ollama) {
        this.ollama = ollama;
    }

    public OpenAi getOpenAi() {
        return openAi;
    }

    public void setOpenAi(OpenAi openAi) {
        this.openAi = openAi;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public int getTokenLimit() {
        return tokenLimit;
    }

    public void setTokenLimit(int tokenLimit) {
        this.tokenLimit = tokenLimit;
    }

    // ------- OLLAMA -------
    public static class Ollama {
        private ChatModel chatModel = new ChatModel();
        private EmbeddingModel embeddingModel = new EmbeddingModel();

        private String tokenizerModelName = "gpt-4o-mini"; // default

        public String getTokenizerModelName() { return tokenizerModelName; }
        public void setTokenizerModelName(String tokenizerModelName) { this.tokenizerModelName = tokenizerModelName; }

        public ChatModel getChatModel() {
            return chatModel;
        }

        public void setChatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
        }

        public EmbeddingModel getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public static class ChatModel {
            private String modelName;      // e.g. "mistral"
            private Double temperature;    // e.g. 0.0

            public String getModelName() {
                return modelName;
            }

            public void setModelName(String modelName) {
                this.modelName = modelName;
            }

            public Double getTemperature() {
                return temperature;
            }

            public void setTemperature(Double temperature) {
                this.temperature = temperature;
            }
        }

        public static class EmbeddingModel {
            private String modelName; // e.g. "mistral"

            public String getModelName() {
                return modelName;
            }

            public void setModelName(String modelName) {
                this.modelName = modelName;
            }
        }
    }

    // ------- OPENAI -------
    // Maps to: langchain4j.open-ai.streaming-chat-model.* and langchain4j.open-ai.embedding-model.*
    public static class OpenAi {
        private StreamingChatModel streamingChatModel = new StreamingChatModel();
        private EmbeddingModel embeddingModel = new EmbeddingModel();

        public StreamingChatModel getStreamingChatModel() {
            return streamingChatModel;
        }

        public void setStreamingChatModel(StreamingChatModel streamingChatModel) {
            this.streamingChatModel = streamingChatModel;
        }

        public EmbeddingModel getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public static class StreamingChatModel {
            private String apiKey;            // ${OPENAI_API_KEY}
            private String modelName;         // e.g. "gpt-4o-mini"
            private Double temperature;       // e.g. 0.0
            private Boolean strictTools;      // optional

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public String getModelName() {
                return modelName;
            }

            public void setModelName(String modelName) {
                this.modelName = modelName;
            }

            public Double getTemperature() {
                return temperature;
            }

            public void setTemperature(Double temperature) {
                this.temperature = temperature;
            }

            public Boolean getStrictTools() {
                return strictTools;
            }

            public void setStrictTools(Boolean strictTools) {
                this.strictTools = strictTools;
            }
        }

        public static class EmbeddingModel {
            private String apiKey;       // ${OPENAI_API_KEY}
            private String modelName;    // e.g. "text-embedding-3-small"

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public String getModelName() {
                return modelName;
            }

            public void setModelName(String modelName) {
                this.modelName = modelName;
            }
        }
    }
}