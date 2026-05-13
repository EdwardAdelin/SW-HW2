package com.example.demo.service;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

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

    public Map<String, List<String>> getBookDetails(String bookLocalName) {
        Map<String, List<String>> details = new HashMap<>();
        String bookUri = NS + bookLocalName;

        // Query for all properties of this specific book
        String queryString =
                "PREFIX ex: <" + NS + "> " +
                        "SELECT ?p ?o WHERE { <" + bookUri + "> ?p ?o . }";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                String property = soln.getResource("p").getLocalName();
                String value = soln.get("o").isResource() ?
                        soln.getResource("o").getLocalName() :
                        soln.get("o").toString();

                details.computeIfAbsent(property, k -> new ArrayList<>()).add(value);
            }
        }
        return details;
    }

    public Map<String, Object> getGraphData() {
        List<Map<String, String>> nodes = new ArrayList<>();
        List<Map<String, String>> edges = new ArrayList<>();
        Set<String> seenNodes = new HashSet<>();

        StmtIterator it = model.listStatements();
        while (it.hasNext()) {
            Statement stmt = it.nextStatement();
            String s = stmt.getSubject().getLocalName();
            String p = stmt.getPredicate().getLocalName();
            String o = stmt.getObject().isResource() ? stmt.getObject().asResource().getLocalName() : stmt.getObject().toString();

            if (seenNodes.add(s)) nodes.add(Map.of("id", s, "label", s));
            if (stmt.getObject().isResource() && seenNodes.add(o)) nodes.add(Map.of("id", o, "label", o));

            edges.add(Map.of("from", s, "to", o, "label", p));
        }
        return Map.of("nodes", nodes, "edges", edges);
    }

    /**
     * Task 2: Parse an uploaded RDF file and return Nodes and Edges for Vis.js
     */
    public Map<String, Object> parseAndGetGraphData(org.springframework.web.multipart.MultipartFile file) {
        Model tempModel = ModelFactory.createDefaultModel();
        List<Map<String, String>> nodes = new ArrayList<>();
        List<Map<String, String>> edges = new ArrayList<>();
        java.util.Set<String> seenNodes = new java.util.HashSet<>();

        try (java.io.InputStream in = file.getInputStream()) {
            // Read the uploaded file into a temporary Jena model
            tempModel.read(in, null);

            StmtIterator it = tempModel.listStatements();
            while (it.hasNext()) {
                Statement stmt = it.nextStatement();

                // Extract Subject, Predicate, Object
                String s = stmt.getSubject().isURIResource() ? stmt.getSubject().getLocalName() : stmt.getSubject().toString();
                String p = stmt.getPredicate().getLocalName();
                String o = stmt.getObject().isResource() ? stmt.getObject().asResource().getLocalName() : stmt.getObject().toString();

                // Add Subject Node
                if (seenNodes.add(s)) {
                    nodes.add(Map.of("id", s, "label", s, "color", "#97C2FC"));
                }

                // Add Object Node (make literals a different color)
                if (seenNodes.add(o)) {
                    if (stmt.getObject().isLiteral()) {
                        nodes.add(Map.of("id", o, "label", o, "shape", "box", "color", "#E2E2E2"));
                    } else {
                        nodes.add(Map.of("id", o, "label", o, "color", "#FB7E81"));
                    }
                }

                // Add Edge connecting them
                edges.add(Map.of("from", s, "to", o, "label", p, "arrows", "to"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Failed to parse RDF file.");
        }

        return Map.of("nodes", nodes, "edges", edges);
    }
}