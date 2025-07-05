-- Create database if it doesn't exist
SELECT 'CREATE DATABASE sms_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sms_db')\gexec

-- Connect to the database
\c sms_db;

-- Create any initial tables or data here if needed
-- The tables will be created automatically by Quarkus/Hibernate