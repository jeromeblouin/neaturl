package org.neaturl.service;

import java.util.Optional;

/**
 * Strategy interface allowing to support more than 1 encoder/decoder implementations.
 */
public interface UrlEncoderStrategy {

    String encode(String url);

    Optional<String> decode(String encodedUrl);
}
