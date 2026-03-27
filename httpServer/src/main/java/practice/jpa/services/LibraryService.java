package practice.jpa.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.jpa.entities.Author;
import practice.jpa.entities.Book;
import practice.jpa.repositories.AuthorRepository;
import practice.jpa.repositories.BookRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class LibraryService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public LibraryService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public Book createBook(String title, List<Long> authorIds) {
        log.info("Creating book. title={}, authorIds={}", title, authorIds);

        List<Author> authors = authorIds == null || authorIds.isEmpty()
                ? new ArrayList<>()
                : authorRepository.findAllById(authorIds);

        Book book = new Book();
        book.setTitle(title);
        book.setAuthors(authors);

        for (Author author : authors) {
            if (author.getBooks() == null) {
                author.setBooks(new ArrayList<>());
            }
            author.getBooks().add(book);
        }

        Book savedBook = bookRepository.save(book);

        log.info("Book created. id={}, title={}, authorsCount={}",
                savedBook.getId(), savedBook.getTitle(), savedBook.getAuthors().size());

        return savedBook;
    }

    public Author createAuthor(String name, List<Long> bookIds) {
        log.info("Creating author. name={}, bookIds={}", name, bookIds);

        List<Book> books = bookIds == null || bookIds.isEmpty()
                ? new ArrayList<>()
                : bookRepository.findAllById(bookIds);

        Author author = new Author();
        author.setName(name);
        author.setBooks(books);

        for (Book book : books) {
            if (book.getAuthors() == null) {
                book.setAuthors(new ArrayList<>());
            }
            book.getAuthors().add(author);
        }

        Author savedAuthor = authorRepository.save(author);

        log.info("Author created. id={}, name={}, booksCount={}",
                savedAuthor.getId(), savedAuthor.getName(), savedAuthor.getBooks().size());

        return savedAuthor;
    }

    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        log.info("Fetching all books");
        List<Book> books = bookRepository.findAll();

        log.info("Fetched {} books", books.size());
        return books;
    }

    @Transactional(readOnly = true)
    public List<Author> getAllAuthors() {
        log.info("Fetching all authors");
        List<Author> authors = authorRepository.findAll();

        log.info("Fetched {} authors", authors.size());
        return authors;
    }

    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        log.info("Fetching book by id={}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found. id={}", id);
                    return new IllegalArgumentException("Book not found: " + id);
                });

        log.info("Book fetched successfully. id={}, title={}", book.getId(), book.getTitle());
        return book;
    }

    @Transactional(readOnly = true)
    public Author getAuthorById(Long id) {
        log.info("Fetching author by id={}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Author not found. id={}", id);
                    return new IllegalArgumentException("Author not found: " + id);
                });

        log.info("Author fetched successfully. id={}, name={}", author.getId(), author.getName());
        return author;
    }

    public Book updateBook(Long id, String title, List<Long> authorIds) {
        log.info("Updating book. id={}, newTitle={}, authorIds={}", id, title, authorIds);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found for update. id={}", id);
                    return new IllegalArgumentException("Book not found: " + id);
                });

        if (title != null && !title.isBlank()) {
            book.setTitle(title);
        }

        if (authorIds != null) {
            List<Author> newAuthors = authorRepository.findAllById(authorIds);

            if (book.getAuthors() != null) {
                for (Author oldAuthor : book.getAuthors()) {
                    if (oldAuthor.getBooks() != null) {
                        oldAuthor.getBooks().remove(book);
                    }
                }
            }

            book.setAuthors(new ArrayList<>(newAuthors));

            for (Author author : newAuthors) {
                if (author.getBooks() == null) {
                    author.setBooks(new ArrayList<>());
                }
                if (!author.getBooks().contains(book)) {
                    author.getBooks().add(book);
                }
            }
        }

        Book updatedBook = bookRepository.save(book);

        log.info("Book updated successfully. id={}, title={}, authorsCount={}",
                updatedBook.getId(), updatedBook.getTitle(), updatedBook.getAuthors().size());

        return updatedBook;
    }

    public Author updateAuthor(Long id, String name, List<Long> bookIds) {
        log.info("Updating author. id={}, newName={}, bookIds={}", id, name, bookIds);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Author not found for update. id={}", id);
                    return new IllegalArgumentException("Author not found: " + id);
                });

        if (name != null && !name.isBlank()) {
            author.setName(name);
        }

        if (bookIds != null) {
            List<Book> newBooks = bookRepository.findAllById(bookIds);

            if (author.getBooks() != null) {
                for (Book oldBook : author.getBooks()) {
                    if (oldBook.getAuthors() != null) {
                        oldBook.getAuthors().remove(author);
                    }
                }
            }

            author.setBooks(new ArrayList<>(newBooks));

            for (Book book : newBooks) {
                if (book.getAuthors() == null) {
                    book.setAuthors(new ArrayList<>());
                }
                if (!book.getAuthors().contains(author)) {
                    book.getAuthors().add(author);
                }
            }
        }

        Author updatedAuthor = authorRepository.save(author);

        log.info("Author updated successfully. id={}, name={}, booksCount={}",
                updatedAuthor.getId(), updatedAuthor.getName(), updatedAuthor.getBooks().size());

        return updatedAuthor;
    }

    public void deleteBook(Long id) {
        log.info("Deleting book. id={}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found for delete. id={}", id);
                    return new IllegalArgumentException("Book not found: " + id);
                });

        if (book.getAuthors() != null) {
            for (Author author : book.getAuthors()) {
                if (author.getBooks() != null) {
                    author.getBooks().remove(book);
                }
            }
        }

        bookRepository.delete(book);

        log.info("Book deleted successfully. id={}", id);
    }

    public void deleteAuthor(Long id) {
        log.info("Deleting author. id={}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Author not found for delete. id={}", id);
                    return new IllegalArgumentException("Author not found: " + id);
                });

        if (author.getBooks() != null) {
            for (Book book : author.getBooks()) {
                if (book.getAuthors() != null) {
                    book.getAuthors().remove(author);
                }
            }
        }

        authorRepository.delete(author);

        log.info("Author deleted successfully. id={}", id);
    }
}