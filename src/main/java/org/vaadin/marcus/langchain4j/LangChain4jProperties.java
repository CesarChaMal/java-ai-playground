package org.vaadin.marcus.langchain4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "langchain4j")
public class LangChain4jProperties {

    private String baseUrl = "http://localhost:11434";
    private Ollama ollama = new Ollama();

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

    public static class Ollama {
        private ChatModel chatModel = new ChatModel();
        private EmbeddingModel embeddingModel = new EmbeddingModel();

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
            private String modelName;
            private Double temperature;

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
            private String modelName;

            public String getModelName() {
                return modelName;
            }

            public void setModelName(String modelName) {
                this.modelName = modelName;
            }
        }
    }
}
