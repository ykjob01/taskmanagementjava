IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'task_management')
BEGIN
    CREATE DATABASE task_management;
END
