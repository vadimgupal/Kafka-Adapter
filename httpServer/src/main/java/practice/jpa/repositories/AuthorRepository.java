package practice.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.jpa.entities.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}