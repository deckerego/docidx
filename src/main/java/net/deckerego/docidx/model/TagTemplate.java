package net.deckerego.docidx.model;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.io.Serializable;
import java.util.Date;

@Document(indexName = "docidx", type = "tagtemplate")
@Mapping(mappingPath = "tagtemplate-mapping.json")
@Setting(settingPath = "docidx-settings.json")
public class TagTemplate implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(TagTemplate.class);

    @Id
    public String id;
    public Mat template;
    public String name;
    public Date indexUpdated;

    public byte[] getTemplate() {
        throw new UnsupportedOperationException("Serialization of TagTemplate in docidx is not supported");
    }

    public void setTemplate(byte[] image) {
        this.template = Imgcodecs.imdecode(new MatOfByte(image), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }
}
