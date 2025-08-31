# Database Setup for Aegis

## Creating the Users Table

Since the application user doesn't have CREATE permissions, you need to run the following SQL manually as a superuser:

```bash
# Connect to PostgreSQL as superuser
sudo -u postgres psql aegis_security

# Run the schema script
\i /home/uttam/IdeaProjects/aegis/src/main/resources/schema.sql
```

Or run this SQL directly:

```sql
-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    active BOOLEAN NOT NULL DEFAULT true,
    address VARCHAR(255),
    contact_person VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    email VARCHAR(255) NOT NULL,
    last_login TIMESTAMP,
    name VARCHAR(255) NOT NULL,
    organization VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN','USER')),
    updated_at TIMESTAMP,
    CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email)
);

-- Grant permissions to aegis user
GRANT ALL PRIVILEGES ON TABLE users TO aegis;
GRANT USAGE, SELECT ON SEQUENCE users_id_seq TO aegis;
```

After creating the table, the application should start successfully.