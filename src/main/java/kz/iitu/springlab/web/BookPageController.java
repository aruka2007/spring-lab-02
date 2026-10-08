package kz.iitu.springlab.web;

import jakarta.servlet.http.HttpServletResponse;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
public class BookPageController {

    private final BookService service;

    public BookPageController(BookService service) {
        this.service = service;
    }

    // Task 3: Render catalogue list page
    @GetMapping
    public String list(@RequestParam(required = false) String author,
                       Model model) {
        model.addAttribute("books", service.findAll(author));
        model.addAttribute("author", author);
        model.addAttribute("title", "Catalogue");
        return "books/list"; // resolves to templates/books/list.html
    }

    // Task 4: Post/Redirect/Get pattern
    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String author,
                         @RequestParam int year,
                         RedirectAttributes redirect) {
        Book saved = service.create(new Book(null, title, author, year));
        redirect.addFlashAttribute("message", "Book «" + saved.title() + "» has been added");
        return "redirect:/books"; // 302 redirect -> browser performs safe GET
    }

    // Individual Variant 10: GET /books/{id} - Single record page with its own template; missing shows not-found
    @GetMapping("/{id}")
    public String findOne(@PathVariable long id, Model model, HttpServletResponse response) {
        return service.findById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("title", "Book Details: " + book.title());
                    return "books/detail"; // resolves to templates/books/detail.html
                })
                .orElseGet(() -> {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    model.addAttribute("missingId", id);
                    model.addAttribute("title", "Book Not Found");
                    return "books/not-found"; // resolves to templates/books/not-found.html
                });
    }
}
