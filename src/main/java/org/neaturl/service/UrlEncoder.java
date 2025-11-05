package org.neaturl.service;

import org.neaturl.service.repository.Url;
import org.neaturl.service.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
public class UrlEncoder {

    private final int BASE = 62;
    private static final Map<Integer, Character> alphabet = new HashMap<Integer, Character>();

    private final UrlRepository urlRepository;

    static {
        AtomicInteger index = new AtomicInteger();
        Stream.of("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
                .forEach(ch -> alphabet.put(index.getAndIncrement(), ch.charAt(0)));
    }

    public UrlEncoder(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String encode(Url url) {
        var savedUrl = urlRepository.save(url);
        var id = savedUrl.getId();

        if (id == 0) {
            return String.valueOf(alphabet.get(0));
        }

        var result = new StringBuilder();
        long remainder;
        while (id > 0) {
            remainder = id % BASE;
            // TODO: Add map validation
            result.append(alphabet.get((int)remainder));
            id = id / BASE;
        }

        return result.reverse().toString();
    }

    public String decode(String encodedUrl) {
        var number = new AtomicLong();
        var index = new AtomicInteger();

        Stream.of(encodedUrl)
                .forEach(ch -> number.set(number.get() * BASE + index.getAndIncrement()));

        return urlRepository
                .findById(number.get())
                .map(Url::getUrl)
                .orElseThrow();
    }
}
