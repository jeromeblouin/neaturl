package org.neaturl.service;

import lombok.extern.slf4j.Slf4j;
import org.neaturl.service.repository.base62.Base62Url;
import org.neaturl.service.repository.base62.Base62UrlRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Primary
@Slf4j
public class Base62UrlEncoder implements UrlEncoderStrategy {

    private static final int BASE = 62;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Map<Character, Integer> alphabetIndexes = new HashMap<>();

    private final Base62UrlRepository urlRepository;

    static {
        for (int i = 0; i < ALPHABET.length(); i++) {
            alphabetIndexes.put(ALPHABET.charAt(i), i);
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
        while (id > 0) {
            result.append(ALPHABET.charAt((int) (id % BASE)));
            id = id / BASE;
        }
        var encodedUrl = result.reverse().toString();

        log.debug("Encoded base62 URL: {}", encodedUrl);
        return encodedUrl;
    }

    public Optional<String> decode(String encodedUrl) {
        long number = 0;

        for (char c : encodedUrl.toCharArray()) {
            var index = alphabetIndexes.get(c);
            if (index == null) {
                throw new InvalidEncodedUrl("Invalid URL: " + c);
            }
            number = number * BASE + index;
        }

        return urlRepository
                .findById(number)
                .map(Base62Url::getUrl);
    }
}
