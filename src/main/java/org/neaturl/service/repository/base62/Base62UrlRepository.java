package org.neaturl.service.repository.base62;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Base62UrlRepository extends JpaRepository<Base62Url, Long> {

    Optional<Base62Url> findByUrl(String url);
}
