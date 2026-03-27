package practice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import practice.dto.CreateAuthorRequest;
import practice.jpa.entities.Author;
import practice.jpa.services.LibraryService;

import java.util.List;

@RestController
@RequestMapping("/authors")
@Slf4j
public class AuthorController {

    private final LibraryService libraryService;

    public AuthorController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping
    public ResponseEntity<Author> createAuthor(@RequestBody CreateAuthorRequest request) {
        log.info("POST /authors called. name={}, bookIds={}", request.getName(), request.getBookIds());
        Author author = libraryService.createAuthor(request.getName(), request.getBookIds());

        log.info("Author created successfully. id={}, name={}", author.getId(), author.getName());
        return ResponseEntity.ok(author);
    }

    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        log.info("GET /authors called");
        List<Author> authors = libraryService.getAllAuthors();

        log.info("Returned {} authors", authors.size());
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        log.info("GET /authors/{} called", id);
        Author author = libraryService.getAuthorById(id);

        log.info("Author found. id={}, name={}", author.getId(), author.getName());
        return ResponseEntity.ok(author);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(
            @PathVariable Long id,
            @RequestBody CreateAuthorRequest request
    ) {
        log.info("PUT /authors/{} called. newName={}, bookIds={}", id, request.getName(), request.getBookIds());
        Author author = libraryService.updateAuthor(id, request.getName(), request.getBookIds());

        log.info("Author updated successfully. id={}, name={}", author.getId(), author.getName());
        return ResponseEntity.ok(author);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        log.info("DELETE /authors/{} called", id);
        libraryService.deleteAuthor(id);

        log.info("Author deleted successfully. id={}", id);
        return ResponseEntity.noContent().build();
    }
}