CREATE TABLE movies (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    genre VARCHAR(100),
    director VARCHAR(200),
    cast_members TEXT,
    duration_minutes INTEGER,
    release_date DATE,
    poster_url VARCHAR(500),
    base_price DECIMAL(10,2) NOT NULL
);