package HoangThiMyThanh.QuanLySach.service;
import HoangThiMyThanh.QuanLySach.entities.Book;
import HoangThiMyThanh.QuanLySach.repositories.IBookRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE,
 rollbackFor = {Exception.class, Throwable.class})
public class BookService {
 private final IBookRepository bookRepository;
 public List<Book> getAllBooks(Integer pageNo, 
 Integer pageSize, 
 String sortBy) {
 return bookRepository.findAllBooks(pageNo, pageSize, sortBy);
 }
 public Optional<Book> getBookById(Long id) {
 return bookRepository.findById(id);
 }
public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    public Book updateBook(@NotNull Book book) {
        Book existingBook = bookRepository.findById(book.getId())
                .orElse(null);
        Objects.requireNonNull(existingBook).setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setCategory(book.getCategory());
        return bookRepository.save(existingBook);
 }

    public List<Book> searchBook(String keyword) {
        return bookRepository.searchBook(keyword);
    }

    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    public long countBooks() {
        return bookRepository.count();
    }
}