package practice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import practice.dto.CreateBookRequest;
import practice.jpa.entities.Book;
import practice.jpa.services.LibraryService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LibraryService libraryService;

    @Test
    void shouldReturnBooks() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        List<Book> books = List.of(book);

        Mockito.when(libraryService.getAllBooks())
                .thenReturn(books);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Book"));
    }

    @Test
    void shouldReturnBookById() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");

        Mockito.when(libraryService.getBookById(1L))
                .thenReturn(book);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void shouldCreateBook() throws Exception {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("New Book");
        request.setAuthorIds(List.of());

        Book savedBook = new Book();
        savedBook.setId(2L);
        savedBook.setTitle("New Book");

        Mockito.when(libraryService.createBook(Mockito.anyString(), Mockito.anyList()))
                .thenReturn(savedBook);

        mockMvc.perform(post("/books")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("New Book"));
    }

    @Test
    void shouldUpdateBook() throws Exception {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Updated Book");
        request.setAuthorIds(List.of());

        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Updated Book");

        Mockito.when(libraryService.updateBook(Mockito.eq(1L), Mockito.anyString(), Mockito.anyList()))
                .thenReturn(updatedBook);

        mockMvc.perform(put("/books/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Book"));
    }

    @Test
    void shouldDeleteBook() throws Exception {
        Mockito.doNothing().when(libraryService).deleteBook(1L);

        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isNoContent());
    }
}