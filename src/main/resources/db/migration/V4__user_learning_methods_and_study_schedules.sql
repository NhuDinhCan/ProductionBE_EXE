IF OBJECT_ID('user_learning_methods', 'U') IS NULL
BEGIN
    CREATE TABLE user_learning_methods (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        career_id BIGINT NOT NULL,
        learning_method_id BIGINT NOT NULL,
        status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
        applied_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT uq_user_learning_method UNIQUE (user_id, career_id, learning_method_id),
        CONSTRAINT fk_user_learning_methods_user FOREIGN KEY (user_id) REFERENCES users(id),
        CONSTRAINT fk_user_learning_methods_career FOREIGN KEY (career_id) REFERENCES career(id),
        CONSTRAINT fk_user_learning_methods_method FOREIGN KEY (learning_method_id) REFERENCES learning_methods(id)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_user_learning_methods_user' AND object_id = OBJECT_ID('user_learning_methods'))
    CREATE INDEX ix_user_learning_methods_user ON user_learning_methods(user_id, applied_at DESC);

IF OBJECT_ID('study_schedules', 'U') IS NULL
BEGIN
    CREATE TABLE study_schedules (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        career_id BIGINT NOT NULL,
        learning_method_id BIGINT NULL,
        title NVARCHAR(255) NOT NULL,
        description NVARCHAR(1000) NULL,
        day_of_week NVARCHAR(20) NULL,
        start_time TIME NULL,
        end_time TIME NULL,
        status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT fk_study_schedules_user FOREIGN KEY (user_id) REFERENCES users(id),
        CONSTRAINT fk_study_schedules_career FOREIGN KEY (career_id) REFERENCES career(id),
        CONSTRAINT fk_study_schedules_method FOREIGN KEY (learning_method_id) REFERENCES learning_methods(id)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_study_schedules_user_day_time' AND object_id = OBJECT_ID('study_schedules'))
    CREATE INDEX ix_study_schedules_user_day_time ON study_schedules(user_id, day_of_week, start_time);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_study_schedules_user_career_method' AND object_id = OBJECT_ID('study_schedules'))
    CREATE INDEX ix_study_schedules_user_career_method ON study_schedules(user_id, career_id, learning_method_id, status);
