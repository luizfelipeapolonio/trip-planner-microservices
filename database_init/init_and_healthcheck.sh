#!/bin/bash

create_postgres_databases() {
  psql -U $POSTGRES_USER -d $POSTGRES_DB -c "CREATE DATABASE trip_planner_user_service;"
  psql -U $POSTGRES_USER -d $POSTGRES_DB -c "CREATE DATABASE trip_planner_trip_service;"
}

echo "Creating databases..."

# Calling the above function
create_postgres_databases

# Function to check postgres health
check_postgres_health() {
  pg_isready -U $POSTGRES_USER
  return $?
}

# Wait for postgres to be ready
until check_postgres_health; do
  echo "Waiting for postgres to be ready.."
  sleep 2
done

echo "Postgres is ready. Running health check..."

#Perform health check by verifying the databases exist
DATABASES=("trip_planner_user_service" "trip_planner_trip_service")

for db in "${DATABASES[@]}"; do
  if psql -U $POSTGRES_USER -d $POSTGRES_DB -c "SELECT 1 FROM pg_database WHERE datname='$db';" | grep -q 1; then
    echo "Database $db exists."
  else
    echo "Database $db does not exist."
    exit 1
  fi
done

echo "All databases exist. Health check passed."
exit 0
