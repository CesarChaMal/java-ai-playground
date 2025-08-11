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

//    @Bean
//    ChatMemoryProvider chatMemoryProvider(Tokenizer tokenizer) {
//        return chatId -> TokenWindowChatMemory.withMaxTokens(1000, tokenizer);

    /// /        return chatId -> MessageWindowChatMemory.withMaxMessages(50);
//    }
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
                .maxResults(2)
                .minScore(0.6)
                .build();
    }

    // -------- OLLAMA PROFILE --------
    @Configuration
    @Profile("ollama")
    static class OllamaProvider {

        private final LangChain4jProperties props;

        @Value("${langchain4j.ollama.tokenizer.model-name:gpt-4o-mini}")
        private String tokenizerModelName;

        OllamaProvider(LangChain4jProperties props) {
            this.props = props;
        }

//        @Bean
        @Bean("ollamaModel")
        public StreamingChatLanguageModel streamingChatModel() {
            return OllamaStreamingChatModel.builder()
                    .baseUrl(props.getBaseUrl())
                    .modelName(props.getOllama().getChatModel().getModelName())
                    .temperature(props.getOllama().getChatModel().getTemperature())
                    .build();
        }

//        @Bean
        @Bean("ollamaEmbeddingModel")
        public EmbeddingModel embeddingModel() {
            return OllamaEmbeddingModel.builder()
                    .baseUrl(props.getBaseUrl())
                    .modelName(props.getOllama().getEmbeddingModel().getModelName())
                    .build();
        }

/*
        @Bean
        dev.langchain4j.model.Tokenizer tokenizer() {
            // Use property-driven model name for token estimation
            return new dev.langchain4j.model.openai.OpenAiTokenizer(tokenizerModelName);
        }
*/

        @Bean
        dev.langchain4j.model.Tokenizer tokenizer() {
            return new dev.langchain4j.model.openai.OpenAiTokenizer(
                    props.getOllama().getTokenizerModelName()
            );
        }

        @Bean
        ChatMemoryProvider chatMemoryProvider(dev.langchain4j.model.Tokenizer tokenizer) {
            return chatId -> TokenWindowChatMemory.withMaxTokens(1000, tokenizer);
        }
    }

    // -------- OPENAI PROFILE --------
    @Configuration
    @Profile("openai")
    static class OpenAiProvider {

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

//        @Bean
        @Bean("openAiModel")
        public StreamingChatLanguageModel streamingChatModel() {
            return OpenAiStreamingChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(openAiModelName)
                    .temperature(openAiTemperature)
                    .build();
        }

//        @Bean
        @Bean("openAiEmbeddingModel")
        public EmbeddingModel embeddingModel() {
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
            return chatId -> TokenWindowChatMemory.withMaxTokens(1000, tokenizer);
        }
    }
}
