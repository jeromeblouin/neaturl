package org.neaturl.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.neaturl.service.repository.hashedurl.HashedUrl;
import org.neaturl.service.repository.hashedurl.HashedUrlRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class HashUrlEncoder implements UrlEncoderStrategy {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final int MAX_HASH_RETRIES = 100;

    private final HashedUrlRepository urlRepository;

    public HashUrlEncoder(HashedUrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String encode(String url) {
        var hash = DigestUtils.sha256Hex(url).substring(0, 8);

        int retries = 0;
        String newHash = hash;
        while (urlRepository.findById(newHash).isPresent() && retries++ < MAX_HASH_RETRIES) {
            char randomChar = ALPHABET.charAt(new Random().nextInt(ALPHABET.length()));
            newHash = hash + randomChar;
        }
        if (retries == 100) {
            throw new IllegalStateException("Unable to create a unique hash for URL " + url);
        }
        hash = newHash;

        urlRepository.save(new HashedUrl(hash, url));
        log.debug("Encoded hashed URL: {}", hash);

        return hash;
    }

    public Optional<String> decode(String encodedUrl) {
        return urlRepository
                .findById(encodedUrl)
                .map(HashedUrl::getUrl);
    }
}
