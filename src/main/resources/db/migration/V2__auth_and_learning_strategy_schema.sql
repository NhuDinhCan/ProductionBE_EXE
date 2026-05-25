IF OBJECT_ID('invalid_tokens', 'U') IS NULL
BEGIN
    CREATE TABLE invalid_tokens (
        id NVARCHAR(255) PRIMARY KEY,
        expiration_time DATETIME2 NOT NULL
    );
END;

IF COL_LENGTH('invalid_tokens', 'token') IS NOT NULL
BEGIN
    ALTER TABLE invalid_tokens ALTER COLUMN token NVARCHAR(MAX) NULL;
END;

IF COL_LENGTH('invalid_tokens', 'expirationTime') IS NOT NULL AND COL_LENGTH('invalid_tokens', 'expiration_time') IS NULL
BEGIN
    EXEC sp_rename 'invalid_tokens.expirationTime', 'expiration_time', 'COLUMN';
END;

IF COL_LENGTH('invalid_tokens', 'expiration_time') IS NOT NULL
BEGIN
    UPDATE invalid_tokens SET expiration_time = SYSUTCDATETIME() WHERE expiration_time IS NULL;
    ALTER TABLE invalid_tokens ALTER COLUMN expiration_time DATETIME2 NOT NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_invalid_tokens_expiration_time' AND object_id = OBJECT_ID('invalid_tokens'))
    CREATE INDEX ix_invalid_tokens_expiration_time ON invalid_tokens(expiration_time);

IF OBJECT_ID('learning_methods', 'U') IS NULL
BEGIN
    CREATE TABLE learning_methods (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title NVARCHAR(255) NOT NULL,
        description NVARCHAR(2000) NOT NULL,
        benefits NVARCHAR(2000) NULL
    );
END;

IF OBJECT_ID('learning_strategy_profiles', 'U') IS NULL
BEGIN
    CREATE TABLE learning_strategy_profiles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        career_id BIGINT NOT NULL UNIQUE,
        description NVARCHAR(2000) NOT NULL,
        skills NVARCHAR(2000) NULL,
        tools NVARCHAR(2000) NULL,
        weekly_roadmap NVARCHAR(4000) NULL,
        CONSTRAINT fk_learning_strategy_profiles_career FOREIGN KEY (career_id) REFERENCES career(id)
    );
END;

IF OBJECT_ID('major_learning_methods', 'U') IS NULL
BEGIN
    CREATE TABLE major_learning_methods (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        career_id BIGINT NOT NULL,
        learning_method_id BIGINT NOT NULL,
        sort_order INT NOT NULL DEFAULT 0,
        CONSTRAINT fk_major_learning_methods_career FOREIGN KEY (career_id) REFERENCES career(id),
        CONSTRAINT fk_major_learning_methods_method FOREIGN KEY (learning_method_id) REFERENCES learning_methods(id),
        CONSTRAINT uq_major_learning_methods UNIQUE (career_id, learning_method_id)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_major_learning_methods_career_sort' AND object_id = OBJECT_ID('major_learning_methods'))
    CREATE INDEX ix_major_learning_methods_career_sort ON major_learning_methods(career_id, sort_order);
