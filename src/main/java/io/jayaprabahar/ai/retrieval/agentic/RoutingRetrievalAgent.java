package io.jayaprabahar.ai.retrieval.agentic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jayaprabahar.ai.retrieval.rag.RAGHelperTool;

@Service
@Slf4j
public class RoutingRetrievalAgent {

    private ChatClient chatClient;
    private final VectorStore vectorStore;
    private final RAGHelperTool ragHelperTool;

    @Value("${application.conversationRememberCount:10}")
    public int conversationRememberCount = 10;

    // Inject the VectorStore and RAG helper so the routing agent can perform RAG-style answers
    public RoutingRetrievalAgent(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, RAGHelperTool ragHelperTool) {
        this.vectorStore = vectorStore;
        this.ragHelperTool = ragHelperTool;

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(conversationRememberCount)
                .build();

        this.chatClient = chatClientBuilder.defaultAdvisors(
                MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build()
        ).build();
    }


    public String identifyRoute(String input) {
        String routerPrompt = String.format(
                """
                        Please analyze the following input and determine the most appropriate return policy from the available options: %s.
                        If the question is not related to return policies, kindly indicate that no specific route is necessary.
                        
                        Begin by explaining your reasoning, then provide your selection using the following JSON structure:
                        
                        {
                            "reasoning": "A concise explanation detailing why this policy should be routed to a specific agent.
                             Consider key terms, user intent, Product purchase date, customer's premium type and urgency level.",
                            "selection": "Name of the selected support team"
                        }
                        
                        Input:
                        %s
                        """,
                Routing.AGENT_ROUTES,
                input
        );


        RoutingResponse routingResponse = chatClient.prompt(routerPrompt).call().entity(RoutingResponse.class);

        assert routingResponse != null;
        // Use SLF4J parameterized logging (avoid printf-style placeholders)
        log.info("Routing Analysis: {}. Selected route: {}", routingResponse.reasoning(), routingResponse.selection());

        String selectedRoute = routingResponse.selection();
        String selectedPrompt = Routing.AGENT_ROUTES.get(selectedRoute);

        // If we found a known route, run the selected prompt through the chat client and wire the vector store
        // so the agent can enrich the response with relevant documents (Agentic RAG behavior).
        if (selectedPrompt != null) {
            return chatClient.prompt("Selected Prompt is " + selectedPrompt + "\nInput: " + input)
                    .call()
                    .content();
        }

        // Fallback: just return the routing reasoning when no route is found
        return String.format("No matching route found. \nRouting analysis: %s", routingResponse.reasoning());
    }
}
