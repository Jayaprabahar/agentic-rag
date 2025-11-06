package io.jayaprabahar.ai.retrieval.agentic;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AgenticRagChatController {

    private final RoutingRetrievalAgent routingRetrievalAgent;

    @GetMapping("/agentic-retrieval-using-routingpattern")
    String queryWithAgenticRag(@RequestParam(value = "message") String userQuery) {
        return routingRetrievalAgent.identifyRoute(userQuery);
    }
}
