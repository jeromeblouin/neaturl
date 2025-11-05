package org.neaturl.service;

public interface UrlEncoderStrategy {

    String encode(String url);

    String decode(String encodedUrl);
}
