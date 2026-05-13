package com.example.demo.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.apache.jena.rdf.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private JenaService jenaService;

    private final EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    @PostConstruct
    public void initVectorDb() {
        Model model = jenaService.getModel();
        StmtIterator it = model.listStatements();
        
        // Loop through RDF and create text descriptions for the Vector DB
        while (it.hasNext()) {
            Statement stmt = it.nextStatement();
            String fact = String.format("The resource %s has property %s with value %s.",
                    stmt.getSubject().getLocalName(),
                    stmt.getPredicate().getLocalName(),
                    stmt.getObject().toString());
            
            // Add to Vector DB
            TextSegment segment = TextSegment.from(fact);
            embeddingStore.add(embeddingModel.embed(segment).content(), segment);
        }
        System.out.println("Vector Database populated with RDF facts!");
    }

    public String getRelevantContext(String query) {
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(embeddingModel.embed(query).content(), 3);
        return matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n"));
    }
}