package com.example.demo.controller;

import com.example.demo.service.ChatService;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    // Inject the ChatService which already built the Vector DB from your RDF
    @Autowired
    private ChatService chatService;

    // Keep your model configuration
    private final GoogleAiGeminiChatModel lmm = GoogleAiGeminiChatModel.builder()
            .apiKey("replace this with your api") // Set your API key here
            .modelName("gemini-3.1-flash-lite") // Recommend using 1.5-flash instead of 3.1
            .build();

    @PostMapping("/ask")
    public String ask(@RequestBody Map<String, String> payload) {
        String userQuery = payload.get("question");

        // 1. Retrieval: Find the most relevant facts from the ChatService Vector DB
        String context = chatService.getRelevantContext(userQuery);
        System.out.println("DEBUG - Context sent to LLM: " + context);

        // 2. Augmentation & Generation: Send the context + question to Gemini
        return lmm.generate("You are a helpful book assistant. Use the following context to answer the user accurately.\n\n" +
                "Context:\n" + context + "\n\n" +
                "User Question: " + userQuery);
    }
}