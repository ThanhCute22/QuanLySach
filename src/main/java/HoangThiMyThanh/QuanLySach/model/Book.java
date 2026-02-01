package HoangThiMyThanh.QuanLySach.model;

public class Book {
private int id;
 private String title;
 private String author;
 private long price;
 private String category;
public String getCategory() { return category; }
public void setCategory(String category) { this.category = category; }
 public Book(){

 }
 
 public int getId() {
    return id;
}
 public void setId(int id) {
    this.id = id;
 }
 public String getTitle() {
    return title;
 }
 public void setTitle(String title) {
    this.title = title;
 }
 public String getAuthor() {
    return author;
 }
 public void setAuthor(String author) {
    this.author = author;
 }
 public long getPrice() {
    return price;
 }
 public void setPrice(long price) {
    this.price = price;
 }
 public Book(int id, String title, String author, long price) {
    this.id = id;
    this.title = title;
    this.author = author;
    this.price = price;
 }

 @Override
 public String toString() {
    return "Book{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", author='" + author + '\'' +
            ", price=" + price +
            '}';
 }
}
