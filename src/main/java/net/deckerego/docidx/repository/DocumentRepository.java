package net.deckerego.docidx.repository;

import net.deckerego.docidx.model.FileEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends ElasticsearchRepository<FileEntry, String> {
    @Query("{\"bool\": {\"must\": [{\"match\": {\"parentPath\": \"?0\"}}]}}")
    Page<FileEntry> findByParentPath(String parentPath, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"parentPath\": \"?0\"}}, {\"match\": {\"fileName\": \"?1\"}}]}}")
    FileEntry findByFilename(String parentPath, String filename);
}
