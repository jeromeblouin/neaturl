package org.neaturl.service.repository;

import jakarta.persistence.*;

@Entity
@Table(name = "url")
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Declare the constraints so that the schema can be auto-generated with needed constraints.
    // This also allows to validate the data before reaching the DB.
    @Column(nullable = false, unique = true)
    private String url;
}
