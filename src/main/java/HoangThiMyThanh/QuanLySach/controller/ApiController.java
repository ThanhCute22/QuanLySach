package HoangThiMyThanh.QuanLySach.controller;

import HoangThiMyThanh.QuanLySach.service.BookService;
import HoangThiMyThanh.QuanLySach.service.CartService;
import HoangThiMyThanh.QuanLySach.service.CategoryService;
import HoangThiMyThanh.QuanLySach.viewmodels.BookGetVm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@lombok.extern.slf4j.Slf4j
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApiController {
 private final BookService bookService;
 private final CategoryService categoryService;
 private final CartService cartService;
 @GetMapping("/books")
 public ResponseEntity<List<BookGetVm>> getAllBooks(Integer pageNo, 
Integer pageSize, String sortBy) {
 return ResponseEntity.ok(bookService.getAllBooks(
 pageNo == null ? 0 : pageNo,
 pageSize == null ? 20 : pageSize,
 sortBy == null ? "id" : sortBy)
 .stream()
 .map(BookGetVm::from)
 .toList());
 }
 @GetMapping({"/books/id/{id}", "/books/{id}"})
 public ResponseEntity<BookGetVm> getBookById(@PathVariable Long id) 
{
 return ResponseEntity.ok(bookService.getBookById(id)
 .map(BookGetVm::from)
 .orElse(null));
 }
 @DeleteMapping("/books/{id}")
 public ResponseEntity<Void> deleteBookById(@PathVariable Long id) {
 bookService.deleteBookById(id);
 return ResponseEntity.ok().build();
 }

 @PostMapping("/books")
 public ResponseEntity<?> createBook(@RequestBody HoangThiMyThanh.QuanLySach.viewmodels.BookPostVm vm) {
     try {
         if (vm == null || vm.categoryId() == null) {
             return ResponseEntity.badRequest().body(Map.of("message", "categoryId is required"));
         }
         var catOpt = categoryService.getCategoryById(vm.categoryId());
         if (catOpt.isEmpty()) {
             return ResponseEntity.badRequest().body(Map.of("message", "category not found"));
         }
         var book = new HoangThiMyThanh.QuanLySach.entities.Book();
         book.setTitle(vm.title());
         book.setAuthor(vm.author());
         book.setPrice(vm.price());
         book.setCategory(catOpt.get());
         var saved = bookService.addBook(book);
         return ResponseEntity.status(201).body(BookGetVm.from(saved));
     } catch (jakarta.validation.ConstraintViolationException ex) {
         // collect validation messages
         var messages = ex.getConstraintViolations()
                 .stream()
                 .map(v -> v.getPropertyPath() + " " + v.getMessage())
                 .toList();
         log.warn("Validation failed for createBook: {}", messages);
         return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", messages));
     } catch (Exception ex) {
         log.error("Failed to create book via API: {}", ex.getMessage(), ex);
         return ResponseEntity.status(500).body(Map.of("message", ex.getMessage()));
     }
 }

 @PutMapping("/books/{id}")
 public ResponseEntity<BookGetVm> updateBook(@PathVariable Long id, @RequestBody HoangThiMyThanh.QuanLySach.viewmodels.BookPostVm vm) {
     var existingOpt = bookService.getBookById(id);
     if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
     if (vm == null || vm.categoryId() == null) return ResponseEntity.badRequest().build();
     var catOpt = categoryService.getCategoryById(vm.categoryId());
     if (catOpt.isEmpty()) return ResponseEntity.badRequest().build();
     var existing = existingOpt.get();
     existing.setTitle(vm.title());
     existing.setAuthor(vm.author());
     existing.setPrice(vm.price());
     existing.setCategory(catOpt.get());
     var saved = bookService.updateBook(existing);
     return ResponseEntity.ok(BookGetVm.from(saved));
 }

 @GetMapping("/books/search")
 public ResponseEntity<List<BookGetVm>> searchBooks(String keyword) 
 {
 return ResponseEntity.ok(bookService.searchBook(keyword)
 .stream()
 .map(BookGetVm::from)
 .toList());
 }

 // ---- Category endpoints ----
 @GetMapping("/categories")
 public ResponseEntity<List<HoangThiMyThanh.QuanLySach.entities.Category>> getAllCategories() {
     return ResponseEntity.ok(categoryService.getAllCategories());
 }

 @GetMapping("/categories/{id}")
 public ResponseEntity<HoangThiMyThanh.QuanLySach.entities.Category> getCategoryById(@PathVariable Long id) {
     return categoryService.getCategoryById(id)
             .map(ResponseEntity::ok)
             .orElseGet(() -> ResponseEntity.notFound().build());
 }

 @PostMapping("/categories")
 public ResponseEntity<HoangThiMyThanh.QuanLySach.entities.Category> createCategory(@RequestBody HoangThiMyThanh.QuanLySach.entities.Category category) {
     var saved = categoryService.saveCategory(category);
     return ResponseEntity.status(201).body(saved);
 }

 @PutMapping("/categories/{id}")
 public ResponseEntity<HoangThiMyThanh.QuanLySach.entities.Category> updateCategory(@PathVariable Long id, @RequestBody HoangThiMyThanh.QuanLySach.entities.Category category) {
     return categoryService.getCategoryById(id)
             .map(existing -> {
                 existing.setName(category.getName());
                 var saved = categoryService.saveCategory(existing);
                 return ResponseEntity.ok(saved);
             }).orElseGet(() -> ResponseEntity.notFound().build());
 }

 @DeleteMapping("/categories/{id}")
 public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
     categoryService.deleteCategoryById(id);
     return ResponseEntity.ok().build();
 }
}

