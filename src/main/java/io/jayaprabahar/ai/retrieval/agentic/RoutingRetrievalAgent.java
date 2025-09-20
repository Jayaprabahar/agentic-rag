package io.jayaprabahar.ai.retrieval.agentic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoutingRetrievalAgent {

    private ChatClient chatClient;

    @Value("${application.conversationRememberCount:10}")
    public int conversationRememberCount = 10;

    public RoutingRetrievalAgent(ChatClient.Builder chatClientBuilder) {
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

        if (selectedPrompt == null) {
            throw new IllegalArgumentException("Selected route '" + selectedRoute + "' not found in routes map");
        }

        // Process the input with the selected prompt
        return chatClient.prompt(selectedPrompt + "\nInput: " + input).call().content();
    }
}
