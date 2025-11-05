package org.neaturl.service;

import java.util.Optional;

public interface UrlEncoderStrategy {

    String encode(String url);

    Optional<String> decode(String encodedUrl);
}
