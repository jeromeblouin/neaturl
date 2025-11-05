package org.neaturl.service;

public class InvalidEncodedUrl extends RuntimeException {

    public InvalidEncodedUrl() {
    }

    public InvalidEncodedUrl(String url) {}
}
