package io.jayaprabahar.ai.retrieval.rag;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Alternative way to load data into Vector Store using Resource file via Java.
 * Enable SpringBoot Service annotation
 */
/*@Service*/
@RequiredArgsConstructor
public class VectorLoader {

    private final VectorStore vectorStore;

    @Value("classpath:/chroma/amazon_return_policy.txt")
    private Resource resource;

    @PostConstruct
    void loadVectorDB(){
        String data;
        try (InputStream inputStream = resource.getInputStream()) {
            data = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List <Document> documents = List.of(new Document(data, Map.of(
                "title", "amazon_return_policy",
                "description", "This policy describes the return policy of Amazon",
                "date", "17-Sep-2025"
                )));

        vectorStore.add(documents);
    }
}
