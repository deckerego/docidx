package net.deckerego.docidx.repository;

import net.deckerego.docidx.model.FileEntry;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends ElasticsearchRepository<FileEntry, String> {
    @Query("{\"bool\": {\"must\": [{\"match\": {\"parentPath\": \"?0\"}}]}}")
    List<FileEntry> findByParentPath(String parent);
}
