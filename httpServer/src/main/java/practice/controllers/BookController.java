package practice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import practice.dto.CreateBookRequest;
import practice.jpa.entities.Book;
import practice.jpa.services.LibraryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/books")
public class BookController {

    private final LibraryService libraryService;

    public BookController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody CreateBookRequest request) {
        log.info("POST /books called. title={}, authorIds={}", request.getTitle(), request.getAuthorIds());
        Book book = libraryService.createBook(request.getTitle(), request.getAuthorIds());
        log.info("Book created successfully. id={}, title={}", book.getId(), book.getTitle());
        return ResponseEntity.ok(book);
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        log.info("GET /books called");
        List<Book> books = libraryService.getAllBooks();

        log.info("Returned {} books", books.size());
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        log.info("GET /books/{} called", id);
        Book book = libraryService.getBookById(id);

        log.info("Book found. id={}, title={}", book.getId(), book.getTitle());
        return ResponseEntity.ok(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @RequestBody CreateBookRequest request
    ) {
        log.info("PUT /books/{} called. newTitle={}, authorIds={}", id, request.getTitle(), request.getAuthorIds());
        Book book = libraryService.updateBook(id, request.getTitle(), request.getAuthorIds());

        log.info("Book updated successfully. id={}, title={}", book.getId(), book.getTitle());
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("DELETE /books/{} called", id);
        libraryService.deleteBook(id);

        log.info("Book deleted successfully. id={}", id);
        return ResponseEntity.noContent().build();
    }
}