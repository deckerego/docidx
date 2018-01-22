package net.deckerego.docidx.model;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

@Document(indexName = "docidx", type = "tagtemplate")
@Mapping(mappingPath = "tagtemplate-mapping.json")
@Setting(settingPath = "docidx-settings.json")
public class TagTemplate implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(TagTemplate.class);

    @Id
    public String id;
    public FImage template;
    public String name;
    public Date indexUpdated;
    public transient TemplateMatcher templateMatcher;

    public byte[] getTemplate() {
        throw new UnsupportedOperationException("Serialization of TagTemplate in docidx is not supported");
    }

    public void setTemplate(byte[] image) {
        BufferedImage templateImage;

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
            this.template = ImageUtilities.readF(inputStream);
        } catch(IOException e) {
            LOG.error(String.format("Could not deserialize thumbnail of %s", this.id), e);
            this.template = ImageUtilities.createFImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        }
    }
}
