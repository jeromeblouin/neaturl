package org.neaturl.service;

import org.neaturl.service.repository.Url;
import org.neaturl.service.repository.UrlRepository;
import org.springframework.stereotype.Service;

@Service
public class UrlEncoder {

    private final UrlRepository urlRepository;

    public UrlEncoder(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String encode(Url url) {
        var savedUrl = urlRepository.save(url);

        return "";
    }
}
