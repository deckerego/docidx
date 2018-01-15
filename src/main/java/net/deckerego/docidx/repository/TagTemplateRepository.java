package net.deckerego.docidx.repository;

import net.deckerego.docidx.model.TagTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagTemplateRepository extends ElasticsearchRepository<TagTemplate, String> {
}
