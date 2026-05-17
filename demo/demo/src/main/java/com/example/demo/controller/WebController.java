package com.example.demo.controller;

import com.example.demo.service.JenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {

    @Autowired
    private JenaService jenaService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("books", jenaService.getAllBooks());
        return "index"; // refers to index.html
    }

    @GetMapping("/book/{name}")
    public String bookDetails(@PathVariable String name, Model model) {
        model.addAttribute("name", name);
        model.addAttribute("details", jenaService.getBookDetails(name));
        return "book-details"; // refers to book-details.html
    }

    @GetMapping("/overview")
    public String showOverview() {
        // This tells Spring to look for "overview.html" in the templates folder
        return "overview";
    }

    // Shows the empty visualization page
    @GetMapping("/visualize")
    public String showVisualizationPage() {
        return "visualize"; // refers to visualize.html
    }

    // Receives the file via AJAX, processes it, and returns JSON
    @org.springframework.web.bind.annotation.PostMapping("/api/upload-rdf")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> handleRdfUpload(
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return jenaService.parseAndGetGraphData(file);
    }
}