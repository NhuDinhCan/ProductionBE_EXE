-- Chạy script này trong SQL Server để tạo bảng còn thiếu
-- (Bảng này được dùng để blacklist token khi logout)

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='invalid_tokens' AND xtype='U')
BEGIN
    CREATE TABLE invalid_tokens (
        id               NVARCHAR(255) PRIMARY KEY,
        expiration_time  DATETIME2 NOT NULL
    );
    PRINT 'Table invalid_tokens created.';
END
ELSE
    PRINT 'Table invalid_tokens already exists.';
