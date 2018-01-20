# DocIndex

DocIndex is the batch process used to feed DocMag, a front-end to Elasticsearch
that allows server-side document searching to be simple.


## Requirements

DocIndex can be run directly on an OS, however it is recommended to be run within
a Docker container. The container is composed within the DocMag docker-compose.yml.

Usually you won't want to build and run docidx locally, instead it is best to
run the docker container published at: https://hub.docker.com/r/deckerego/docidx/


## Building and Testing Locally

Building and testing can be performed locally with Maven and Spring Boot:

    mvn spring-boot:run

If you would also like to spin up a local Elasticsearch and Kibana instance for
testing, you can deploy both with Docker configs in the `tests/` directory:

    cd tests
    docker-compose up -d


## Searching and Querying Documents

To search within your documents, use DocMag available at https://github.com/deckerego/docmag

You could also query Elasticsearch directly using the API or Kibana's dev tools. A query sent over the API might be:

    GET /docidx/_search
    {"query": { "simple_query_string" :
      { "query": "water bill" }
    }}
