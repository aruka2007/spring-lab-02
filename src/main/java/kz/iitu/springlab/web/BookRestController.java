package kz.iitu.springlab.web;

import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService service;

    public BookRestController(BookService service) {
        this.service = service;
    }

    // 1. List books with optional filter and limit
    @GetMapping
    public List<Book> list(@RequestParam(name = "author", required = false) String author,
                           @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return service.findAll(author).stream().limit(limit).toList();
    }

    // 2. Read single book by id (throws BookNotFoundException -> ApiExceptionHandler)
    @GetMapping("/{id}")
    public Book find(@PathVariable("id") long id) {
        return service.getById(id);
    }

    // 3. Create book
    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        Book saved = service.create(book);
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.id()))
                .body(saved);
    }

    // 4. Replace / update book (TODO 2.2)
    @PutMapping("/{id}")
    public ResponseEntity<Book> replace(@PathVariable("id") long id, @RequestBody Book book) {
        return service.update(id, book)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 5. Delete book (TODO 2.3)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
