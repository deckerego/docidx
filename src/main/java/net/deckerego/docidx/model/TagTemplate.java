package net.deckerego.docidx.model;

import boofcv.io.image.ConvertRaster;
import boofcv.struct.image.GrayF32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.imageio.ImageIO;
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
    public GrayF32 template;
    public String name;
    public Date indexUpdated;

    public byte[] getTemplate() {
        throw new UnsupportedOperationException("Serialization of TagTemplate in docidx is not supported");
    }

    public void setTemplate(byte[] image) {
        BufferedImage templateImage;

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
            templateImage = ImageIO.read(inputStream);
        } catch(IOException e) {
            LOG.error(String.format("Could not deserialize thumbnail of %s", this.id), e);
            templateImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }

        this.template = new GrayF32(templateImage.getWidth(), templateImage.getHeight());
        ConvertRaster.bufferedToGray(templateImage, this.template);
    }
}
