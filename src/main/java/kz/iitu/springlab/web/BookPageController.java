package kz.iitu.springlab.web;

import jakarta.validation.Valid;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.catalog.Genre;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    public String list(@RequestParam(name = "author", required = false) String author,
                       Model model) {
        model.addAttribute("books", service.findAll(author));
        model.addAttribute("author", author);
        model.addAttribute("title", "Catalogue");
        return "books/list";
    }

    // Task 1.4: Render empty form for new book
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new BookForm());
        model.addAttribute("genres", Genre.values());
        model.addAttribute("isEdit", false);
        return "books/form";
    }

    // Task 2.4: Accept form submission with validation and PRG
    @PostMapping
    public String create(@Valid @ModelAttribute("form") BookForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("genres", Genre.values());
            model.addAttribute("isEdit", false);
            return "books/form";
        }
        Book saved = service.create(new Book(null, form.getTitle(), form.getAuthor(), form.getYear()));
        redirect.addFlashAttribute("message", "Book «" + saved.title() + "» has been added");
        return "redirect:/books";
    }

    // Individual Variant 10: GET /books/{id}/edit - populated from record, reusing books/form.html
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") long id, Model model) {
        Book book = service.getById(id);
        BookForm form = new BookForm(book.title(), book.author(), book.year(), Genre.FICTION, true);
        model.addAttribute("form", form);
        model.addAttribute("genres", Genre.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("bookId", id);
        return "books/form";
    }

    // Individual Variant 10: POST /books/{id}/edit - updates record with validation
    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") long id,
                         @Valid @ModelAttribute("form") BookForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("genres", Genre.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("bookId", id);
            return "books/form";
        }
        Book updated = new Book(id, form.getTitle(), form.getAuthor(), form.getYear());
        service.update(id, updated);
        redirect.addFlashAttribute("message", "Book «" + updated.title() + "» has been updated");
        return "redirect:/books/" + id;
    }

    // Read single book detail or trigger PageExceptionHandler when missing
    @GetMapping("/{id}")
    public String findOne(@PathVariable("id") long id, Model model) {
        Book book = service.getById(id);
        model.addAttribute("book", book);
        model.addAttribute("title", "Book Details: " + book.title());
        return "books/detail";
    }
}
