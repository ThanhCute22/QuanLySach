-- Insert categories (Unicode handled via N'...')
INSERT INTO Category (name)
VALUES 
    (N'Lập trình'),
    (N'Phát triển web'),
    (N'Phát triển ứng dụng di động'),
    (N'Trí tuệ nhân tạo'),
    (N'Học máy (Machine Learning)'),
    (N'Cơ sở dữ liệu'),
    (N'Điện toán đám mây'),
    (N'An ninh mạng'),
    (N'Blockchain và tiền mã hóa'),
    (N'Kỹ thuật phần mềm');

-- Optional additional categories
INSERT INTO Category (name) VALUES (N'Big Data');
INSERT INTO Category (name) VALUES (N'DevOps');
INSERT INTO Category (name) VALUES (N'UI/UX Design');

-- Sample books (adjust category_id as appropriate in your DB)
INSERT INTO Book (author, price, title, category_id)
VALUES 
    (N'HOàng Thị Mỹ Thanh', 15, N'Code Dạo Ký Sự - Lập trình viên đâu chỉ biết code', 1),
    (N'Nguyễn Thái Duy', 18, N'The Pragmatic Programmer (dịch tiếng Việt)', 1),
    (N'Robert C. Martin', 14, N'Clean Code - Viết code sạch và đẹp', 1),
    (N'Martin Fowler', 16, N'Refactoring - Cải tiến mã nguồn', 1),
    (N'Bruce Schneier', 17, N'Áp dụng mật mã học trong an ninh mạng', 2),
    (N'Kevin Mitnick', 13, N'Nghệ thuật xâm nhập (The Art of Deception)', 2),
    (N'Nguyễn Văn A', 12, N'An ninh mạng cơ bản cho doanh nghiệp', 2),
    (N'Abraham Silberschatz', 15, N'Cơ sở dữ liệu (Database System Concepts)', 3),
    (N'Elmasri & Navathe', 14, N'Hệ quản trị cơ sở dữ liệu', 3),
    (N'Trần Văn B', 11, N'Hệ thống thông tin quản lý hiện đại', 3),
    (N'Andrew S. Tanenbaum', 16, N'Mạng máy tính (Computer Networks)', 4),
    (N'Larry L. Peterson', 15, N'Mạng máy tính: Một cách tiếp cận hệ thống', 4),
    (N'Wes McKinney', 14, N'Python for Data Analysis (Phân tích dữ liệu với Python)', 5),
    (N'Aurélien Géron', 18, N'Hands-On Machine Learning with Scikit-Learn (dịch)', 5),
    (N'Nguyễn Thị C', 13, N'Khoa học dữ liệu ứng dụng với R và Python', 5);
