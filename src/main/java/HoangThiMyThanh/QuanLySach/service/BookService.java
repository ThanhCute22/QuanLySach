package HoangThiMyThanh.QuanLySach.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import HoangThiMyThanh.QuanLySach.model.Book;

@Service
public class BookService {
//Khai báo danh sách các quyển sách
private List<Book> books = new ArrayList<>();

//Lấy danh sách tất cả các quyển sách
public List<Book> getAllBooks() {
    return books;   
}

//Lấy danh sách theo ID
public Book getBookById(int id) {
    for (Book book : books) {
        if (book.getId() == id) {
            return book;
        }
    }
    return null; // Trả về null nếu không tìm thấy sách với ID đã cho
}


//Thêm quyển sách mới
public void addBook(Book book) {
    books.add(book);
}

//Cập nhật thông tin quyển sách
public boolean updateBook(int id, Book updatedBook) {
    for (int i = 0; i < books.size(); i++) {
        if (books.get(i).getId() == id) {
            books.set(i, updatedBook);
            return true; // Cập nhật thành công
        }
    }
    return false; // Không tìm thấy sách với ID đã cho
}

//xoá sách theo id
public boolean deleteBook(int id) {
    for (int i = 0; i < books.size(); i++) {
        if (books.get(i).getId() == id) {
            books.remove(i);
            return true; // Xoá thành công
        }
    }
    return false; // Không tìm thấy sách với ID đã cho
}


}