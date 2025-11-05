package org.neaturl.service;

import lombok.extern.slf4j.Slf4j;
import org.neaturl.service.repository.Url;
import org.neaturl.service.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.rmi.UnexpectedException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
@Slf4j
public class UrlEncoder {

    private final int BASE = 62;
    private static final List<String> alphabet = new ArrayList<>();
    private static final Map<String, Integer> alphabetIndexes = new HashMap<>();

    private final UrlRepository urlRepository;

    static {
        var index = new AtomicInteger();
        Stream.of("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".split(""))
                .forEach(ch -> {
                    alphabet.add(ch);
                    alphabetIndexes.put(ch, index.getAndIncrement());
                });
    }

    public UrlEncoder(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String encode(String url) {
        var savedUrl = urlRepository.save(new Url(url));
        log.debug("Saved URL entity: {}", savedUrl);
        var id = savedUrl.getId();

        if (id == 0) {
            return String.valueOf(alphabet.get(0));
        }

        var result = new StringBuilder();
        long remainder;
        while (id > 0) {
            remainder = id % BASE;
            // TODO: Add map validation
            result.append(alphabet.get((int) remainder));
            id = id / BASE;
        }
        var encodedUrl = result.reverse().toString();

        log.debug("Encoded URL: {}", encodedUrl);
        return encodedUrl;
    }

    public String decode(String encodedUrl) {
        var number = new AtomicLong();

        Stream.of(encodedUrl.split(""))
            .forEach(ch -> number.set(number.get() * BASE + alphabetIndexes.get(ch)));
        return urlRepository
                .findById(number.get())
                .map(Url::getUrl)
                .orElseThrow(() -> new IllegalArgumentException(encodedUrl));
    }
}
