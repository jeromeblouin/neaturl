package org.neaturl.service;

import lombok.extern.slf4j.Slf4j;
import org.neaturl.service.repository.base62.Base62Url;
import org.neaturl.service.repository.base62.Base62UrlRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
@Primary
@Slf4j
public class Base62UrlEncoder implements UrlEncoderStrategy {

    private final int BASE = 62;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Map<String, Integer> alphabetIndexes = new HashMap<>();

    private final Base62UrlRepository urlRepository;

    static {
        for (int i = 0; i < ALPHABET.length(); i++) {
            alphabetIndexes.put(String.valueOf(ALPHABET.charAt(i)), i);
        }
    }

    public Base62UrlEncoder(Base62UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String encode(String url) {
        var savedUrl = urlRepository.save(new Base62Url(url));
        log.debug("Saved URL entity: {}", savedUrl);
        var id = savedUrl.getId();

        if (id == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        var result = new StringBuilder();
        long remainder;
        while (id > 0) {
            remainder = id % BASE;
            result.append(ALPHABET.charAt((int) remainder));
            id = id / BASE;
        }
        var encodedUrl = result.reverse().toString();

        log.debug("Encoded base62 URL: {}", encodedUrl);
        return encodedUrl;
    }

    public String decode(String encodedUrl) {
        var number = new AtomicLong();

        Stream.of(encodedUrl.split(""))
            .forEach(ch -> {
                if (alphabetIndexes.containsKey(ch)) {
                    number.set(number.get() * BASE + alphabetIndexes.get(ch));
                } else {
                    throw new InvalidEncodedUrl("Invalid URL: " + ch);
                }
            });
        return urlRepository
                .findById(number.get())
                .map(Base62Url::getUrl)
                .orElseThrow(() -> new InvalidEncodedUrl(encodedUrl));
    }
}
