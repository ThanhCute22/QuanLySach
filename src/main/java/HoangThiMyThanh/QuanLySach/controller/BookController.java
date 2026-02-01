package HoangThiMyThanh.QuanLySach.controller;

import HoangThiMyThanh.QuanLySach.model.Book;
import HoangThiMyThanh.QuanLySach.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")  // Trang web chính: /books
public class BookController {

    @Autowired
    private BookService bookService;

    // Trang danh sách sách (web view)
    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "book/list";  // templates/book/list.html
    }

    // Form thêm sách
    @GetMapping("/add")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "book/add";
    }

    // Xử lý thêm sách từ form
    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book) {
        bookService.addBook(book);
        return "redirect:/books";
    }

    // Form sửa sách
    @GetMapping("/edit/{id}")
    public String editBookForm(@PathVariable int id, Model model) {
        Book book = bookService.getBookById(id);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        return "book/edit";
    }

    // Xử lý sửa sách từ form
    @PostMapping("/edit")
    public String updateBook(@ModelAttribute Book book) {
        bookService.updateBook(book.getId(), book);
        return "redirect:/books";
    }

    // Xóa sách
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable int id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }
}