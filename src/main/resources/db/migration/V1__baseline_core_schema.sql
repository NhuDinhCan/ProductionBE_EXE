IF OBJECT_ID('roles', 'U') IS NULL
BEGIN
    CREATE TABLE roles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(255) NOT NULL UNIQUE
    );
END;

IF OBJECT_ID('users', 'U') IS NULL
BEGIN
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        email NVARCHAR(255) NOT NULL UNIQUE,
        password NVARCHAR(255) NOT NULL,
        first_name NVARCHAR(255) NOT NULL,
        last_name NVARCHAR(255) NOT NULL,
        address NVARCHAR(255) NULL,
        birthday DATE NULL,
        phone NVARCHAR(20) NULL,
        avatar NVARCHAR(255) NULL
    );
END;

IF OBJECT_ID('user_has_role', 'U') IS NULL
BEGIN
    CREATE TABLE user_has_role (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NULL,
        role_id BIGINT NULL,
        CONSTRAINT fk_user_has_role_user FOREIGN KEY (user_id) REFERENCES users(id),
        CONSTRAINT fk_user_has_role_role FOREIGN KEY (role_id) REFERENCES roles(id),
        CONSTRAINT uq_user_has_role UNIQUE (user_id, role_id)
    );
END;

IF OBJECT_ID('career', 'U') IS NULL
BEGIN
    CREATE TABLE career (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(255) NULL
    );
END;

IF OBJECT_ID('question', 'U') IS NULL
BEGIN
    CREATE TABLE question (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        content NVARCHAR(MAX) NULL
    );
END;

IF OBJECT_ID('question_career_weight', 'U') IS NULL
BEGIN
    CREATE TABLE question_career_weight (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        question_id BIGINT NULL,
        career_id BIGINT NULL,
        weight FLOAT NULL,
        CONSTRAINT fk_question_career_weight_question FOREIGN KEY (question_id) REFERENCES question(id),
        CONSTRAINT fk_question_career_weight_career FOREIGN KEY (career_id) REFERENCES career(id)
    );
END;

IF OBJECT_ID('university', 'U') IS NULL
BEGIN
    CREATE TABLE university (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(255) NULL,
        region NVARCHAR(255) NULL,
        type NVARCHAR(255) NULL,
        tuition_fee FLOAT NULL
    );
END;

IF OBJECT_ID('university_major', 'U') IS NULL
BEGIN
    CREATE TABLE university_major (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        university_id BIGINT NULL,
        career_id BIGINT NULL,
        score_required FLOAT NULL,
        CONSTRAINT fk_university_major_university FOREIGN KEY (university_id) REFERENCES university(id),
        CONSTRAINT fk_university_major_career FOREIGN KEY (career_id) REFERENCES career(id)
    );
END;

IF OBJECT_ID('conversation', 'U') IS NULL
BEGIN
    CREATE TABLE conversation (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NULL,
        mentor_id BIGINT NULL,
        created_at DATETIME2 NULL,
        CONSTRAINT uq_conversation_participants UNIQUE (user_id, mentor_id)
    );
END;

IF OBJECT_ID('message', 'U') IS NULL
BEGIN
    CREATE TABLE message (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        conversation_id BIGINT NOT NULL,
        sender_email NVARCHAR(255) NOT NULL,
        content NVARCHAR(MAX) NULL,
        created_at DATETIME2 NULL
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_message_conversation_created_at' AND object_id = OBJECT_ID('message'))
    CREATE INDEX ix_message_conversation_created_at ON message(conversation_id, created_at DESC);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_university_major_career_score' AND object_id = OBJECT_ID('university_major'))
    CREATE INDEX ix_university_major_career_score ON university_major(career_id, score_required DESC);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_question_career_weight_question' AND object_id = OBJECT_ID('question_career_weight'))
    CREATE INDEX ix_question_career_weight_question ON question_career_weight(question_id);
