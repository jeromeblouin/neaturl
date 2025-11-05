package org.neaturl.service.repository.hashedurl;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "urlhashkey")
@Data
@NoArgsConstructor
public class HashedUrl {

    @Id
    private String id;

    // Declare the constraints so that the schema can be auto-generated with needed constraints.
    // This also allows to validate the data before reaching the DB.
    @Column(nullable = false)
    private String url;

    public HashedUrl(String hash, String url) {
        this.id = hash;
        this.url = url;
    }
}
