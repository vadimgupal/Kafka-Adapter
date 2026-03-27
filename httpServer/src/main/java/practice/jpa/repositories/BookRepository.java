package practice.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.jpa.entities.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}