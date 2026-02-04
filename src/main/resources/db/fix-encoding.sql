-- fix-encoding.sql
-- SAFE script to check and (optionally) convert varchar columns to NVARCHAR in SQL Server
-- Usage:
-- 1) Open in SSMS and inspect results of the "check" sections.
-- 2) Set @applyChanges = 1 only when you understand and want to apply conversions.
-- 3) Run in a transaction or test on a copy first.

SET NOCOUNT ON;

-- Toggle: set to 1 to apply the conversion steps
DECLARE @applyChanges BIT = 0;

PRINT '=== 1) Current column types (Book.title/author, Category.name) ===';
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE (TABLE_NAME = 'Book' AND COLUMN_NAME IN ('title', 'author'))
   OR (TABLE_NAME = 'Category' AND COLUMN_NAME = 'name');

PRINT '=== 2) Suspicious rows (questions marks or replacement chars) ===';
SELECT id, title, author, DATALENGTH(title) AS title_bytes, LEN(title) AS title_chars
FROM Book
WHERE title LIKE '%?%' OR author LIKE '%?%' OR title LIKE '%�%' OR author LIKE '%�%';

PRINT '=== 2b) Rows containing non-ASCII characters (possible Vietnamese chars) ===';
-- pattern [^ -~] finds characters outside ASCII printable range
SELECT id, title, author
FROM Book
WHERE title LIKE '%[^ -~]%' OR author LIKE '%[^ -~]%';

PRINT '=== 3) Summary of byte vs char lengths (helps detect encoding differences) ===';
SELECT id, title, DATALENGTH(title) AS bytes, LEN(title) AS chars, author, DATALENGTH(author) AS author_bytes, LEN(author) AS author_chars
FROM Book
ORDER BY id;

PRINT '=== 4) Create backups (timestamped) ===';
DECLARE @ts NVARCHAR(20) = REPLACE(CONVERT(CHAR(19), GETDATE(), 120), ':', '') -- yyyymmdd hhmmss
EXEC('SELECT * INTO Book_backup_' + @ts + ' FROM Book');
EXEC('SELECT * INTO Category_backup_' + @ts + ' FROM Category');
PRINT 'Backups created as Book_backup_' + @ts + ' and Category_backup_' + @ts;

-- Option A: Direct ALTER (simpler) - use if you are confident and no blocking constraints
IF @applyChanges = 1
BEGIN
    PRINT '=== APPLY: Direct ALTER columns to NVARCHAR ===';
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Example: change to NVARCHAR(255) and keep NOT NULL if appropriate
        ALTER TABLE Book ALTER COLUMN title NVARCHAR(255) NULL; -- change NULL/NOT NULL as needed
        ALTER TABLE Book ALTER COLUMN author NVARCHAR(255) NULL;
        ALTER TABLE Category ALTER COLUMN name NVARCHAR(255) NULL;

        COMMIT TRANSACTION;
        PRINT 'Direct ALTER completed.';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        PRINT 'Error during ALTER: ' + ERROR_MESSAGE();
    END CATCH
END

-- Option B: Safe copy (add new columns, copy data, then swap)
IF @applyChanges = 1
BEGIN
    PRINT '=== APPLY: Safe copy method (add _new, copy, swap) ===';
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Add new columns
        ALTER TABLE Book ADD title_new NVARCHAR(255) NULL, author_new NVARCHAR(255) NULL;
        UPDATE Book SET title_new = CAST(title AS NVARCHAR(255)), author_new = CAST(author AS NVARCHAR(255));

        -- verify manually before dropping old columns
        PRINT 'Verify Book.title_new and Book.author_new values before swapping.';

        -- If everything OK, drop old and rename
        ALTER TABLE Book DROP COLUMN title; -- may fail if used in indexes/constraints
        EXEC sp_rename 'Book.title_new', 'title', 'COLUMN';

        ALTER TABLE Book DROP COLUMN author; -- may fail if used in indexes/constraints
        EXEC sp_rename 'Book.author_new', 'author', 'COLUMN';

        -- Category
        ALTER TABLE Category ADD name_new NVARCHAR(255) NULL;
        UPDATE Category SET name_new = CAST(name AS NVARCHAR(255));

        ALTER TABLE Category DROP COLUMN name; -- may fail if used in constraints
        EXEC sp_rename 'Category.name_new', 'name', 'COLUMN';

        COMMIT TRANSACTION;
        PRINT 'Safe copy completed.';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        PRINT 'Error during safe copy: ' + ERROR_MESSAGE();
    END CATCH
END

PRINT '=== 5) Re-check column types and some rows ===';
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE (TABLE_NAME = 'Book' AND COLUMN_NAME IN ('title', 'author'))
   OR (TABLE_NAME = 'Category' AND COLUMN_NAME = 'name');

SELECT TOP (50) id, title, author FROM Book ORDER BY id;

PRINT 'Script finished. If data still shows "?" then original data may have been lost (non-recoverable) and must be re-inserted using a Unicode-aware import (use N''prefix in INSERTs).';

-- End of file
