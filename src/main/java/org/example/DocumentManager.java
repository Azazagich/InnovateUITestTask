package org.example;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */

@Slf4j
public class DocumentManager {

    private final List<Document> documentStorage = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {

        if (document.getId() == null || document.getId().isEmpty()){
            document.setId(UUID.randomUUID().toString());
            log.info("Generated new ID for document: {}", document.getId());
        }
        if (document.getCreated() == null){
            document.setCreated(Instant.now());
            log.info("Set creation time for document with ID {}: {}", document.getId(), document.getCreated());
        }
        else {
            documentStorage.removeIf(doc -> doc.getId().equals(document.getId()));
        }
        log.info("Added new document with ID: {}", document.getId());
        documentStorage.add(document);
        return document;
    }


    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> save(SearchRequest request) {
        log.info("Starting search with request: {}", request);

        if (request == null) {
            log.info("Search request is null, returning all documents");
            return new ArrayList<>(documentStorage);
        }

        List<Document> result = documentStorage.stream()
                .filter(document -> {
                    boolean matchesTitle = request.getTitlePrefixes() == null || request.getTitlePrefixes().isEmpty() ||
                            request.getTitlePrefixes().stream().anyMatch(prefix -> document.getTitle() != null && document.getTitle().startsWith(prefix));

                    boolean matchesContent = request.getContainsContents() == null || request.getContainsContents().isEmpty() ||
                            request.getContainsContents().stream()
                                    .anyMatch(content -> document.getContent() != null && document.getContent().contains(content));

                    boolean matchesAuthor = request.getAuthorIds() == null || request.getAuthorIds().isEmpty() ||
                            request.getAuthorIds().stream()
                                    .anyMatch(authorId -> document.getAuthor() != null && authorId.equals(document.getAuthor().getId()));

                    boolean matchesFrom = request.getCreatedFrom() == null ||
                            (document.getCreated() != null && !document.getCreated().isBefore(request.getCreatedFrom()));

                    boolean matchesTo = request.getCreatedTo() == null ||
                            (document.getCreated() != null && !document.getCreated().isAfter(request.getCreatedTo()));

                    return matchesTitle && matchesContent && matchesAuthor && matchesFrom && matchesTo;
                })
                .collect(Collectors.toList());

        log.info("Search completed, found {} documents", result.size());
        return result;
   }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (id == null || id.isEmpty()){
            log.warn("Attempted to find document with invalid type id: {}", id);
            return Optional.empty();
        }
        return documentStorage.stream()
                .filter(doc -> id.equals(doc.getId()))
                .findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
