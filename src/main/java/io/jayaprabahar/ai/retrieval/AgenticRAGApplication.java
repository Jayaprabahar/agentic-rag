package io.jayaprabahar.ai.retrieval;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AgenticRAGApplication {

    @Value("${spring.ai.openai.api.key}")
    public String embeddingAiKey;

	public static void main(String[] args) {
		SpringApplication.run(AgenticRAGApplication.class, args);
	}

    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel(OpenAiApi.builder().apiKey(embeddingAiKey).build());
    }

}
