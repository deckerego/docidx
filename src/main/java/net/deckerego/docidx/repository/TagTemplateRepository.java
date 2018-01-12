package net.deckerego.docidx.repository;

import net.deckerego.docidx.model.TagTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagTemplateRepository extends ElasticsearchRepository<TagTemplate, String> {
    //This doesn't necessarily need to be in Elasticsearch - it can just as easily be in an RDBMS.
    //Currently this is stored as a document simply so we don't have to add yet another piece of infrastructure
}
