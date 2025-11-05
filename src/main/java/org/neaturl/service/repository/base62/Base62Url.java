package org.neaturl.service.repository.base62;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "url")
@Data
@NoArgsConstructor
public class Base62Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Declare the constraints so that the schema can be auto-generated with needed constraints.
    // This also allows to validate the data before reaching the DB.
    @Column(nullable = false)
    private String url;

    public Base62Url(String url) {
        this.url = url;
    }
}
