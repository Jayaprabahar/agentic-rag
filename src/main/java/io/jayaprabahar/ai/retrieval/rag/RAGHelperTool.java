package io.jayaprabahar.ai.retrieval.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RAGHelperTool {

    @Value("${application.similarityTopK:10}")
    public int similarityTopK;

    private final VectorStore vectorStore;

    @Tool(name = "ReturnPolicyTool",
            description = "Get the related return policy details from the vector store. Takes query string as input.")
    public List<Document> returnPolicyQuery(@ToolParam(description = "query") String query) {

        return this.vectorStore
                .similaritySearch(
                        SearchRequest.builder()
                                .query(query)
                                .topK(similarityTopK)
                                .build()
                );

    }

}
