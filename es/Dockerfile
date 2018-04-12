FROM docker.elastic.co/elasticsearch/elasticsearch:5.6.8

MAINTAINER john@deckerego.net

RUN mkdir -p /usr/share/elasticsearch/backups
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-phonetic
COPY --chown=elasticsearch:elasticsearch elasticsearch.yml /usr/share/elasticsearch/config/
