package org.vaadin.marcus.langchain4j;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;

@Configuration
@EnableConfigurationProperties(LangChain4jProperties.class)
public class LangChain4jConfig {

    private final LangChain4jProperties props;

    public LangChain4jConfig(LangChain4jProperties props) {
        this.props = props;
    }

/*
    @Bean
    public StreamingChatLanguageModel streamingChatModel() {
        return OllamaStreamingChatModel.builder()
                .baseUrl(props.getBaseUrl())
                .modelName(props.getOllama().getChatModel().getModelName())
                .temperature(props.getOllama().getChatModel().getTemperature())
                .build();
    }


    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(props.getBaseUrl())
                .modelName(props.getOllama().getEmbeddingModel().getModelName())
                .build();
    }
*/

    @Bean
    ChatMemoryProvider chatMemoryProvider(Tokenizer tokenizer) {
        return chatId -> TokenWindowChatMemory.withMaxTokens(1000, tokenizer);
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
                .maxResults(2)
                .minScore(0.6)
                .build();
    }
}
