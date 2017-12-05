package net.deckerego.docidx.repository;

import net.deckerego.docidx.model.FileEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends CrudRepository<FileEntry, String> {
    List<FileEntry> findByParentPath(String parent);
}
