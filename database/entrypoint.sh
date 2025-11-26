#!/bin/bash
set -e

# Start SQL Server in background
echo "Starting SQL Server..."
/opt/mssql/bin/sqlservr &

# Wait for SQL Server to be ready
echo "Waiting for SQL Server to start..."
for i in {1..60}; do
    if /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P '@@@SQLserver' -Q "SELECT 1" &> /dev/null; then
        echo "SQL Server is ready!"
        break
    fi
    echo "Waiting... ($i/60)"
    sleep 1
done

# Run initialization script
echo "Running initialization script..."
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P '@@@SQLserver' -i /init.sql

echo "Database initialization completed successfully!"

# Keep container running
wait
