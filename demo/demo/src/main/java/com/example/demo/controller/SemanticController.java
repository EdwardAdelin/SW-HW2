package com.example.demo.controller;

import com.example.demo.service.JenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SemanticController {

    @Autowired
    private JenaService jenaService;

    // Endpoint to trigger Task 3: Add Harry Potter
    @GetMapping("/api/add-harry-potter")
    public String addHarryPotter() {
        jenaService.addHarryPotter();
        return "Harry Potter added successfully to the RDF model!";
    }

    // Endpoint to trigger Task 3: Modify Hunger Games
    @GetMapping("/api/modify-hunger-games")
    public String modifyHungerGames() {
        jenaService.modifyHungerGamesReadingLevel();
        return "Hunger Games reading level updated to Intermediate!";
    }

    // Endpoint to trigger Task 4: List Books
    @GetMapping("/api/books")
    public List<String> listBooks() {
        return jenaService.getAllBooks();
    }
}