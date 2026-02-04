package HoangThiMyThanh.QuanLySach.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import HoangThiMyThanh.QuanLySach.validators.annotations.ValidCategoryId; 

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 255, nullable = false)
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @NotBlank(message = "Title must not be blank")
    private String title;

    @Column(name = "author", length = 255, nullable = false)
    @Size(min = 1, max = 255, message = "Author must be between 1 and 255 characters")
    @NotBlank(message = "Author must not be blank")
    private String author;

    @Column(name = "price")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @ValidCategoryId
    @ToString.Exclude
    private Category category;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ItemInvoice> itemInvoices = new ArrayList<>();
 @Override
 public boolean equals(Object o) {
 if (this == o) return true;
 if (o == null || Hibernate.getClass(this) != 
Hibernate.getClass(o)) return false;
 Book book = (Book) o;
 return getId() != null && Objects.equals(getId(), 
book.getId());
 }
 @Override
 public int hashCode() {
 return getClass().hashCode();
 }
}