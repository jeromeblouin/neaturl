package org.neaturl.service;

import org.neaturl.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlEncoder {

    private final UrlRepository urlRepository;

    public UrlEncoder(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String save(String url) {
        return urlRepository.save(url);
    }

    public Optional<String> get(long id) {
        return urlRepository.findById(id);
    }
}
