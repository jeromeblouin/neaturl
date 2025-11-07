package org.neaturl.service;

import lombok.extern.slf4j.Slf4j;
import org.neaturl.service.repository.base62.Base62Url;
import org.neaturl.service.repository.base62.Base62UrlRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Encoder implementation based on the Base62 algorithm.
 * To encode, the mapped URL is progressively built by mapping characters resolved from the modulus calculation of
 * each character in the URL.
 * Each mapped URL is persisted in a database with a numeric key.
 * NOTE:
 * This encoder implementation is recommended over the hash one since this hash solution can cause hash collisions
 * and the handling of collisions require more processing and finding a unique hash with retries is not even guaranteed.
 */
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

    /**
     * Encode the passed in URL
     * @param url To encode.
     * @return The encoded URL.
     */
    public String encode(String url) {
        // Check if the shortcut for the received URL already exists in the database.
        // Multiple same URLs must be resolved to the same shortcut.
        var foundUrl = urlRepository.findByUrl(url);
        if (foundUrl.isPresent()) {
            log.info("URL {} already encoded.", url);
            return encodeNumber(foundUrl.get().getId());
        }

        var savedUrl = urlRepository.save(new Base62Url(url));
        log.debug("Saved URL entity: {}", savedUrl);
        var id = savedUrl.getId();

        // Optimization
        if (id == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        var encodedUrl = encodeNumber(id);

        decode(encodedUrl)
            .ifPresentOrElse(fetchedUrl -> {
                if (!fetchedUrl.equals(url)) {
                    throw new EncodingException(
                            "The decoded URL is not identical to the original one. Decoded URL: " + fetchedUrl);
                }},
                () -> {
                    throw new EncodingException("Encoded URL not found for " + url);
                }
            );


        log.debug("Encoded base62 URL: {}", encodedUrl);
        return encodedUrl;
    }

    /**
     * Decode the passed in encoded URL.
     * @param encodedUrl To decode.
     * @return The decoded URL or Optional.empty() is no result was found in the database.
     */
    public Optional<String> decode(String encodedUrl) {
        long number = 0;

        for (char c : encodedUrl.toCharArray()) {
            var index = alphabetIndexes.get(c);
            if (index == null) {
                throw new EncodingException("Invalid URL: " + c);
            }
            number = number * BASE + index;
        }

        return urlRepository
                .findById(number)
                .map(Base62Url::getUrl);
    }

    private static String encodeNumber(Long number) {
        var result = new StringBuilder();
        while (number > 0) {
            result.append(ALPHABET.charAt((int) (number % BASE)));
            number = number / BASE;
        }
        return result.reverse().toString();
    }
}
