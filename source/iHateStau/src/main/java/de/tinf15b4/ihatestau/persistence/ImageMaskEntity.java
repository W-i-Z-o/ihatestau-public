package de.tinf15b4.ihatestau.persistence;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class ImageMaskEntity implements Serializable {
    @Id
    private String id;

    @Column
    @Lob
    private byte[] imageData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public ImageMaskEntity() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageMaskEntity that = (ImageMaskEntity) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return Arrays.equals(imageData, that.imageData);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(imageData);
        return result;
    }
}
