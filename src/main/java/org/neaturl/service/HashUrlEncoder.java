package org.neaturl.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.neaturl.service.repository.hashedurl.HashedUrl;
import org.neaturl.service.repository.hashedurl.HashedUrlRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class HashUrlEncoder implements UrlEncoderStrategy {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final int MAX_HASH_LENGTH = 8;
    public static final int MAX_HASH_RETRIES = 100;

    private final HashedUrlRepository urlRepository;
    private final Random random;

    public HashUrlEncoder(HashedUrlRepository urlRepository) {
        this(urlRepository, new Random());
    }

    HashUrlEncoder(HashedUrlRepository urlRepository, Random random) {
        this.urlRepository = urlRepository;
        this.random = random;
    }

    public String encode(String url) {
        var hash = DigestUtils.sha256Hex(url).substring(0, MAX_HASH_LENGTH);

        int retries = 0;
        var newHash = hash;
        while (urlRepository.findById(newHash).isPresent() && retries++ < MAX_HASH_RETRIES) {
            char randomChar = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
            newHash = DigestUtils.sha256Hex(hash + randomChar).substring(0, MAX_HASH_LENGTH);
        }
        if (retries == MAX_HASH_RETRIES) {
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
