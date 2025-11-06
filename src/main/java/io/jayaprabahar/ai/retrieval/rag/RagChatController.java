package io.jayaprabahar.ai.retrieval.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RagChatController {

    public static final String PROMPT = """
                    You are a senior customer support person talking to humans about the return policy of the company.
                    Please use advisors to fetch and advice return policies in a polite manner
            """;

    private final VectorStore vectorStore;
    private final RAGHelperTool RAGHelperTool;
    private final ChatClient chatClient;

    @Value("${application.conversationRememberCount:10}")
    public int conversationRememberCount = 10;

    @Value("${application.similarityTopK:10}")
    public int similarityTopK;

    @Value("${application.forbiddenWords}")
    public List<String> forbiddenWords;

    @Value("${openai.api-key:${spring.ai.openai.api-key:}}")
    public String openaiApiKey;

    @Value("${openai.base-url:${spring.ai.openai.base-url:}}")
    public String openaiBaseUrl;

    public RagChatController(VectorStore vectorStore, ChatClient.Builder chatClientBuilder, RAGHelperTool RAGHelperTool) {
        this.vectorStore = vectorStore;
        this.RAGHelperTool = RAGHelperTool;

        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(conversationRememberCount).build();
        this.chatClient = chatClientBuilder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }

    @GetMapping("/refer-documents")
    public List<Document> referDocuments(@RequestParam(value = "message") String message) {
        return this.vectorStore
                .similaritySearch(
                        SearchRequest.builder()
                                .query(message)
                                .topK(similarityTopK)
                                .build()
                );

    }

    @GetMapping("/rag-refer-documents")
    String referDocumentsWithRag(@RequestParam(value = "message") String userInput,
                                 @RequestParam(value = "conversationId", defaultValue = "001") String conversationId) {

        return this.chatClient.prompt(PROMPT)
                .user(userInput)
                .advisors(a ->
                        a.param("conversationId", conversationId))
                .tools(RAGHelperTool)
                .call()
                .content();
    }

    @GetMapping("rag-query-documents")
    String questionAnswerWithDocuments(@RequestParam(value = "message") String userInput,
                                       @RequestParam(value = "conversationId", defaultValue = "001") String conversationId) {

        return this.chatClient.prompt(PROMPT)
                .user(userInput)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .tools(RAGHelperTool)
                .advisors(a -> a.param("conversationId", conversationId))
                .call()
                .content();
    }

    @GetMapping("rag-query-documents-with-sensitivity")
    String questionAnswerWithDocumentsSensitively(@RequestParam(value = "message") String userInput,
                                                  @RequestParam(value = "conversationId", defaultValue = "001") String conversationId) {

        SafeGuardAdvisor safeGuardAdvisor = new SafeGuardAdvisor(forbiddenWords);

        return this.chatClient.prompt(PROMPT)
                .user(userInput)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build(), safeGuardAdvisor)
                .advisors(a -> a.param("conversationId", conversationId))
                .tools(RAGHelperTool)
                .call()
                .content();
    }

   /* @GetMapping("query-via-openaiclient")
    String queryWithOpenAIClient(@RequestParam(value = "message") String userQuery,
                                 @RequestParam(value = "conversationId", defaultValue = "001") String conversationId) {

        // Configures using the `openai.apiKey`, `openai.baseUrl` system properties or application properties
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(openaiApiKey)
                .baseUrl(openaiBaseUrl)
                .build();

        List<Document> contextDocs = vectorStore.similaritySearch(userQuery);
        String context = contextDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        String prompt = "Use the following context to answer:\n" + context + "\nQuestion: " + userQuery;

        ResponseCreateParams params = ResponseCreateParams.builder()
                .input(prompt)
                .conversation(conversationId)
                .model(ChatModel.GPT_4_TURBO)
                .build();
        Response response = client.responses().create(params);

        return response.toString();
    }*/
}
