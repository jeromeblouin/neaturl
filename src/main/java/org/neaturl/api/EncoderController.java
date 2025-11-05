package org.neaturl.api;

import lombok.extern.slf4j.Slf4j;
import org.neaturl.service.InvalidEncodedUrl;
import org.neaturl.service.UrlEncoderStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class EncoderController {

    private final UrlEncoderStrategy encoder;

    public EncoderController(UrlEncoderStrategy encoder) {
        this.encoder = encoder;
    }

    @GetMapping("encode")
    public ResponseEntity<String> encode(@RequestParam String url) {
        log.debug("URL to encode: {}", url);
        return ResponseEntity.ok(encoder.encode(url));
    }

    @GetMapping("decode")
    public ResponseEntity<String> decode(@RequestParam String url) {
        log.debug("URL to decode: {}", url);
        var decodedUrl = encoder.decode(url);
        if (decodedUrl.isPresent()) {
            log.debug("Decoded URL: {}", decodedUrl.get());
            return ResponseEntity.ok(decodedUrl.get());
        } else {
            return ResponseEntity.ok("Invalid encoded URL: " + url);
        }
    }
}
