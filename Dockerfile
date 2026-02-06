FROM mysql:8.0

WORKDIR /docker-entrypoint-initdb.d/

# Copy initialization scripts to the entrypoint directory
COPY ./init .

RUN chown -R mysql:mysql .