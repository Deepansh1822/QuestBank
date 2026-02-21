package in.sfp.main.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Organisations")
@Getter
@Setter
public class Organisation {

    @Id
    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "logo", columnDefinition = "LONGBLOB")
    private byte[] logo;

    @Column(name = "contentType")
    private String contentType;

    public Organisation() {
    }

    public Organisation(String name, byte[] logo, String contentType) {
        this.name = name;
        this.logo = logo;
        this.contentType = contentType;
    }
}
