package practice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import practice.dto.CreateAuthorRequest;
import practice.jpa.entities.Author;
import practice.jpa.services.LibraryService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LibraryService libraryService;

    @Test
    void shouldReturnAuthors() throws Exception {
        Author author = new Author();
        author.setId(1L);
        author.setName("Test");
        List<Author> authors = List.of(author);

        Mockito.when(libraryService.getAllAuthors())
                .thenReturn(authors);

        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test"));
    }

    @Test
    void shouldReturnAuthorById() throws Exception {
        Author author = new Author();
        author.setId(1L);
        author.setName("Test");

        Mockito.when(libraryService.getAuthorById(1L))
                .thenReturn(author);

        mockMvc.perform(get("/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void shouldCreateAuthor() throws Exception {
        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setName("New Author");
        request.setBookIds(List.of());

        Author savedAuthor = new Author();
        savedAuthor.setId(2L);
        savedAuthor.setName("New Author");

        Mockito.when(libraryService.createAuthor(Mockito.anyString(), Mockito.anyList()))
                .thenReturn(savedAuthor);

        mockMvc.perform(post("/authors")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("New Author"));
    }

    @Test
    void shouldUpdateAuthor() throws Exception {
        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setName("Updated Author");
        request.setBookIds(List.of());

        Author updatedAuthor = new Author();
        updatedAuthor.setId(1L);
        updatedAuthor.setName("Updated Author");

        Mockito.when(libraryService.updateAuthor(Mockito.eq(1L), Mockito.anyString(), Mockito.anyList()))
                .thenReturn(updatedAuthor);

        mockMvc.perform(put("/authors/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Author"));
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        Mockito.doNothing().when(libraryService).deleteAuthor(1L);

        mockMvc.perform(delete("/authors/1"))
                .andExpect(status().isNoContent());
    }
}