package practice.jpa.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import practice.jpa.entities.Author;
import practice.jpa.entities.Book;
import practice.jpa.repositories.AuthorRepository;
import practice.jpa.repositories.BookRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LibraryServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBook_shouldSaveBook() {
        Author author = new Author();
        author.setId(1L);

        when(authorRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Book book = libraryService.createBook("Test Book", List.of(1L));

        assertThat(book.getTitle()).isEqualTo("Test Book");
        assertThat(book.getAuthors()).contains(author);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createAuthor_shouldSaveAuthor() {
        Book book = new Book();
        book.setId(1L);

        when(bookRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(book));
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Author author = libraryService.createAuthor("Test Author", List.of(1L));

        assertThat(author.getName()).isEqualTo("Test Author");
        assertThat(author.getBooks()).contains(book);
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void getAllBooks_shouldReturnBooks() {
        Book book = new Book();
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> books = libraryService.getAllBooks();

        assertThat(books).containsExactly(book);
    }

    @Test
    void getAllAuthors_shouldReturnAuthors() {
        Author author = new Author();
        when(authorRepository.findAll()).thenReturn(List.of(author));

        List<Author> authors = libraryService.getAllAuthors();

        assertThat(authors).containsExactly(author);
    }

    @Test
    void getBookById_existing_shouldReturnBook() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book found = libraryService.getBookById(1L);

        assertThat(found).isEqualTo(book);
    }

    @Test
    void getBookById_missing_shouldThrow() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> libraryService.getBookById(1L));
    }

    @Test
    void getAuthorById_existing_shouldReturnAuthor() {
        Author author = new Author();
        author.setId(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        Author found = libraryService.getAuthorById(1L);

        assertThat(found).isEqualTo(author);
    }

    @Test
    void getAuthorById_missing_shouldThrow() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> libraryService.getAuthorById(1L));
    }

    @Test
    void updateBook_shouldModifyBook() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(List.of(2L))).thenReturn(List.of(new Author()));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        Book updated = libraryService.updateBook(1L, "New Title", List.of(2L));

        assertThat(updated.getTitle()).isEqualTo("New Title");
        assertThat(updated.getAuthors()).hasSize(1);
    }

    @Test
    void updateAuthor_shouldModifyAuthor() {
        Author author = new Author();
        author.setId(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.findAllById(List.of(2L))).thenReturn(List.of(new Book()));
        when(authorRepository.save(any(Author.class))).thenAnswer(i -> i.getArgument(0));

        Author updated = libraryService.updateAuthor(1L, "New Name", List.of(2L));

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getBooks()).hasSize(1);
    }

    // ---------- DELETE ----------

    @Test
    void deleteBook_existing_shouldCallDelete() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        libraryService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_missing_shouldThrow() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> libraryService.deleteBook(1L));
    }

    @Test
    void deleteAuthor_existing_shouldCallDelete() {
        Author author = new Author();
        author.setId(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        libraryService.deleteAuthor(1L);

        verify(authorRepository).delete(author);
    }

    @Test
    void deleteAuthor_missing_shouldThrow() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> libraryService.deleteAuthor(1L));
    }
}