FROM postgres

COPY ./database_init/init_and_healthcheck.sh /docker-entrypoint-initdb.d/

# Make all scripts in the directory executable
RUN chmod -R +x /docker-entrypoint-initdb.d
