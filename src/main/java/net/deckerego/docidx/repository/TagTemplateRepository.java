package net.deckerego.docidx.repository;

import org.opencv.core.Mat;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class TagTemplateRepository {
    public Map<Mat, String> getAllTemplates() {
        return new HashMap<>();
    }
}
