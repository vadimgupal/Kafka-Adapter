package practice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import practice.jpa.entities.Author;
import practice.jpa.repositories.AuthorRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(TestcontainersConfig.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthorIntegrationTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void shouldSaveAuthor() {
        Author author = new Author();
        author.setName("Test");

        Author saved = authorRepository.save(author);

        assertNotNull(saved.getId());
    }
}