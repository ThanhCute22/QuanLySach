-- Drop FKs referencing Book, Category, User then drop tables (safe for repeated runs)
-- 1) Drop FKs that reference Book
IF OBJECT_ID('dbo.Book','U') IS NOT NULL
BEGIN
    DECLARE @sql NVARCHAR(MAX) = N'';
    SELECT @sql = @sql + N'ALTER TABLE ['+OBJECT_SCHEMA_NAME(parent_object_id)+'].['+OBJECT_NAME(parent_object_id)+'] DROP CONSTRAINT ['+name+'];\n'
    FROM sys.foreign_keys
    WHERE referenced_object_id = OBJECT_ID('dbo.Book');
    IF @sql <> N'' EXEC sp_executesql @sql;
END
GO

-- 2) Drop FKs that reference Category
IF OBJECT_ID('dbo.Category','U') IS NOT NULL
BEGIN
    DECLARE @sql2 NVARCHAR(MAX) = N'';
    SELECT @sql2 = @sql2 + N'ALTER TABLE ['+OBJECT_SCHEMA_NAME(parent_object_id)+'].['+OBJECT_NAME(parent_object_id)+'] DROP CONSTRAINT ['+name+'];\n'
    FROM sys.foreign_keys
    WHERE referenced_object_id = OBJECT_ID('dbo.Category');
    IF @sql2 <> N'' EXEC sp_executesql @sql2;
END
GO

-- 3) Drop FKs that reference User (or app_user)
IF OBJECT_ID('dbo.app_user','U') IS NOT NULL OR OBJECT_ID('dbo.[user]','U') IS NOT NULL
BEGIN
    DECLARE @sql3 NVARCHAR(MAX) = N'';
    SELECT @sql3 = @sql3 + N'ALTER TABLE ['+OBJECT_SCHEMA_NAME(parent_object_id)+'].['+OBJECT_NAME(parent_object_id)+'] DROP CONSTRAINT ['+name+'];\n'
    FROM sys.foreign_keys
    WHERE referenced_object_id IN (OBJECT_ID('dbo.app_user'), OBJECT_ID('dbo.[user]'));
    IF @sql3 <> N'' EXEC sp_executesql @sql3;
END
GO

-- 4) Drop dependent tables (safe order)
IF OBJECT_ID('dbo.item_invoice','U') IS NOT NULL
    DROP TABLE dbo.item_invoice;
GO
IF OBJECT_ID('dbo.invoices','U') IS NOT NULL
    DROP TABLE dbo.invoices;
GO
IF OBJECT_ID('dbo.Book','U') IS NOT NULL
    DROP TABLE dbo.Book;
GO
IF OBJECT_ID('dbo.Category','U') IS NOT NULL
    DROP TABLE dbo.Category;
GO
IF OBJECT_ID('dbo.app_user','U') IS NOT NULL
    DROP TABLE dbo.app_user;
GO
IF OBJECT_ID('dbo.[user]','U') IS NOT NULL
    DROP TABLE dbo.[user];
GO

-- 5) Optional: verify
SELECT 'Tables dropped (if existed)' as Info;
GO