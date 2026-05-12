package com.example.demo.service;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class JenaService {

    // The namespace must match exactly what we used in the XML/RDF file
    private static final String NS = "http://example.org/bookrec#";
    private static final String RDF_FILE_PATH = "src/main/resources/books.rdf";
    private Model model;

    public JenaService() {
        // Initialize an empty in-memory model and load the RDF file into it
        model = ModelFactory.createDefaultModel();
        loadRdfFile();
    }

    private void loadRdfFile() {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("books.rdf");
            if (in != null) {
                model.read(in, null);
                System.out.println("RDF file loaded successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Task 3 (Part 1): Add a new book "Harry Potter"
     */
    public void addHarryPotter() {
        // 1. Create the resource URIs
        Resource bookClass = model.createResource(NS + "Book");
        Resource harryPotter = model.createResource(NS + "HarryPotter");

        Resource fantasyTheme = model.createResource(NS + "Fantasy");
        Resource beginnerLevel = model.createResource(NS + "Beginner");

        Property hasTheme = model.createProperty(NS + "hasTheme");
        Property suitableFor = model.createProperty(NS + "suitableFor");

        // 2. Add the triples to the model
        harryPotter.addProperty(RDF.type, bookClass)
                .addProperty(hasTheme, fantasyTheme)
                .addProperty(suitableFor, beginnerLevel);

        saveModelToFile();
    }

    /**
     * Task 3 (Part 2): Change reading level for "Hunger Games"
     */
    public void modifyHungerGamesReadingLevel() {
        Resource hungerGames = model.getResource(NS + "HungerGames");
        Property suitableFor = model.getProperty(NS + "suitableFor");
        Resource intermediateLevel = model.createResource(NS + "Intermediate");

        // 1. Remove the old reading level property
        hungerGames.removeAll(suitableFor);

        // 2. Add the new reading level property
        hungerGames.addProperty(suitableFor, intermediateLevel);

        saveModelToFile();
    }

    /**
     * Task 4 (Part 1): List all available books using a SPARQL query
     */
    public List<String> getAllBooks() {
        List<String> books = new ArrayList<>();

        // SPARQL Query to find all subjects (?book) that are of type Book
        String queryString =
                "PREFIX ex: <" + NS + "> " +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "SELECT ?book WHERE { " +
                        "  ?book rdf:type ex:Book . " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource book = soln.getResource("book");
                // Get the local name (e.g., "Dune" instead of "http://example.org/bookrec#Dune")
                books.add(book.getLocalName());
            }
        }
        return books;
    }

    // Helper method to persist changes back to the file
    private void saveModelToFile() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getter for the model (we will need this for the graph visualization later)
    public Model getModel() {
        return model;
    }
}