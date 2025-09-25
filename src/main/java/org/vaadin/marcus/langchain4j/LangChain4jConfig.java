package org.vaadin.marcus.langchain4j;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties(LangChain4jProperties.class)
public class LangChain4jConfig {

    private final LangChain4jProperties props;

    public LangChain4jConfig(LangChain4jProperties props) {
        this.props = props;
    }


    @Bean
    EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }


    @Bean
    ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(props.getMaxResults())
                .minScore(props.getMinScore())
                .build();
    }

    // -------- OLLAMA PROFILE --------
    @Configuration
    @Profile("ollama")
    static class OllamaProvider {

        private final LangChain4jProperties props;



        OllamaProvider(LangChain4jProperties props) {
            this.props = props;
        }

        @Bean
        public StreamingChatLanguageModel streamingChatModel() {
            String modelName = props.getOllama().getChatModel().getModelName();
            if (modelName == null || modelName.trim().isEmpty()) {
                throw new IllegalArgumentException("Ollama chat model name is required but not provided");
            }
            return OllamaStreamingChatModel.builder()
                    .baseUrl(props.getBaseUrl())
                    .modelName(modelName)
                    .temperature(props.getOllama().getChatModel().getTemperature())
                    .build();
        }

        @Bean
        public EmbeddingModel embeddingModel() {
            String modelName = props.getOllama().getEmbeddingModel().getModelName();
            if (modelName == null || modelName.trim().isEmpty()) {
                throw new IllegalArgumentException("Ollama embedding model name is required but not provided");
            }
            return OllamaEmbeddingModel.builder()
                    .baseUrl(props.getBaseUrl())
                    .modelName(modelName)
                    .build();
        }



        @Bean
        dev.langchain4j.model.Tokenizer tokenizer() {
            return new dev.langchain4j.model.openai.OpenAiTokenizer(
                    props.getOllama().getTokenizerModelName()
            );
        }

        @Bean
        ChatMemoryProvider chatMemoryProvider(dev.langchain4j.model.Tokenizer tokenizer) {
            return chatId -> TokenWindowChatMemory.withMaxTokens(props.getTokenLimit(), tokenizer);
        }
    }

    // -------- OPENAI PROFILE --------
    @Configuration
    @Profile("openai")
    static class OpenAiProvider {

        private final LangChain4jProperties props;

        @Value("${langchain4j.open-ai.streaming-chat-model.api-key}")
        private String openAiApiKey;

        @Value("${langchain4j.open-ai.streaming-chat-model.model-name}")
        private String openAiModelName;

        @Value("${langchain4j.open-ai.streaming-chat-model.temperature:0}")
        private double openAiTemperature;

        @Value("${langchain4j.open-ai.embedding-model.api-key}")
        private String openAiEmbeddingApiKey;

        @Value("${langchain4j.open-ai.embedding-model.model-name:text-embedding-3-small}")
        private String openAiEmbeddingModelName;

        OpenAiProvider(LangChain4jProperties props) {
            this.props = props;
        }

        @Bean
        public StreamingChatLanguageModel streamingChatModel() {
            if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("OpenAI API key is required but not provided");
            }
            return OpenAiStreamingChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(openAiModelName)
                    .temperature(openAiTemperature)
                    .build();
        }

        @Bean
        public EmbeddingModel embeddingModel() {
            if (openAiEmbeddingApiKey == null || openAiEmbeddingApiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("OpenAI Embedding API key is required but not provided");
            }
            return OpenAiEmbeddingModel.builder()
                    .apiKey(openAiEmbeddingApiKey)
                    .modelName(openAiEmbeddingModelName)
                    .build();
        }

        // Add Tokenizer for token-based memory
        @Bean
        dev.langchain4j.model.Tokenizer tokenizer() {
            return new dev.langchain4j.model.openai.OpenAiTokenizer(openAiModelName);
        }

        @Bean
        ChatMemoryProvider chatMemoryProvider(dev.langchain4j.model.Tokenizer tokenizer) {
            return chatId -> TokenWindowChatMemory.withMaxTokens(props.getTokenLimit(), tokenizer);
        }
    }
}
