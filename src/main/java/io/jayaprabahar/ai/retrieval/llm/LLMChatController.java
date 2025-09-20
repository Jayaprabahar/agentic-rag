package io.jayaprabahar.ai.retrieval.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LLMChatController {

    public static final String PROMPT = "Deliver short, informative responses suitable for quick decision-making";

    @Value("${application.conversationRememberCount:10}")
    public int conversationRemember = 10;

    private ChatClient chatClient;

    public LLMChatController(ChatClient.Builder chatClientBuilder) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(conversationRemember)
                .build();

        this.chatClient = chatClientBuilder.defaultAdvisors(
                MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build()
        ).build();
    }

    @GetMapping("/llm-support-chat")
    String generation(@RequestParam(value = "message", defaultValue = "Hello LLM") String userInput,
                      @RequestParam(value = "conversationId", defaultValue = "001") String conversationId) {
        return this.chatClient.prompt(PROMPT)
                .user(userInput)
                .advisors(advisorSpec -> advisorSpec.param("conversationId", conversationId))
                .call()
                .content();
    }
}