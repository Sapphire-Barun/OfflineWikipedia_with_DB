-- 1. Books Table (ISBN has hyphens, so VARCHAR is correct here)
CREATE TABLE IF NOT EXISTS books (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    author VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);

-- 2. Members Table (Use INT to match Java 'int id')
CREATE TABLE IF NOT EXISTS members (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 3. Lending Records Table (member_id MUST also be INT to match members.id)
CREATE TABLE IF NOT EXISTS lending_records (
    record_id SERIAL PRIMARY KEY,
    isbn VARCHAR(20) REFERENCES books(isbn) ON DELETE CASCADE,
    member_id INT REFERENCES members(id) ON DELETE CASCADE,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE
);