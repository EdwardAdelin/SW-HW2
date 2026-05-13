package com.example.demo.controller;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    // Task 7.1: Setup the Vector Database (In-Memory)
    private final EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    // Task 7.4: API Key hardcoded directly into the model builder
    private final GoogleAiGeminiChatModel lmm = GoogleAiGeminiChatModel.builder()
            .apiKey("AIzaSyBMLxHZeKkWGrGoWo45TTo00GgaZS9hRPo")
            .modelName("gemini-3.1-flash-lite") // Use 2.0 instead of 1.5
            .build();

    public ChatController() {
        // Pre-populating the Vector DB with your RDF-based facts
        //ingestData("Dune is written by Frank Herbert. Its theme is Science Fiction and Fantasy. Suitable for Advanced.");
        ingestData("Dune is written by Gigel, a famous developer from Bucharest. Crazy dude.");
        ingestData("The Silent Patient is written by Alex Michaelides. Its theme is Mystery and Murder. Suitable for Intermediate.");
        ingestData("Hunger Games is written by Suzanne Collins. Its theme is Science Fiction. Suitable for Beginner.");
        // Add a "Gigel" fact to prove RAG works during your presentation!
        ingestData("The secret developer of this project is Gigel.");
    }

    private void ingestData(String text) {
        TextSegment segment = TextSegment.from(text);
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);
    }

    @PostMapping("/ask")
    public String ask(@RequestBody Map<String, String> payload) {
        String userQuery = payload.get("question");

        // 1. Retrieval: Find the most relevant fact from our local Vector DB
        Embedding queryEmbedding = embeddingModel.embed(userQuery).content();
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);

        String context = relevant.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n"));
        System.out.println("DEBUG - Context sent to LLM: " + context);

        // 2. Augmentation & Generation: Send the context + question to Gemini
        return lmm.generate("You are a helpful book assistant. Use the following context to answer the user accurately.\n\n" +
                "Context:\n" + context + "\n\n" +
                "User Question: " + userQuery);
    }
}