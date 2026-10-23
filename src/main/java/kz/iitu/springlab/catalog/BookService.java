package kz.iitu.springlab.catalog;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        if (author == null || author.isBlank()) {
            return repository.findAll();
        }
        return repository.findAll().stream()
                .filter(b -> b.author() != null && b.author().toLowerCase().contains(author.toLowerCase()))
                .toList();
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book getById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book) {
        boolean duplicate = repository.findAll().stream()
                .anyMatch(b -> b.title().equalsIgnoreCase(book.title()) && b.author().equalsIgnoreCase(book.author()));
        if (duplicate) {
            throw new DuplicateTitleException(book.title());
        }
        return repository.save(new Book(null, book.title(), book.author(), book.year(),
                book.genre() != null ? book.genre() : Genre.FICTION,
                book.createdAt() != null ? book.createdAt() : Instant.now()));
    }

    public Book update(long id, Book book) {
        if (repository.findById(id).isEmpty()) {
            throw new BookNotFoundException(id);
        }
        Book updated = new Book(id, book.title(), book.author(), book.year(),
                book.genre() != null ? book.genre() : Genre.FICTION,
                book.createdAt() != null ? book.createdAt() : Instant.now());
        return repository.save(updated);
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }
}
