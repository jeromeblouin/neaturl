package org.neaturl.api;

import lombok.extern.slf4j.Slf4j;
import org.neaturl.service.UrlEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class EncoderController {

    private final UrlEncoder encoder;

    public EncoderController(UrlEncoder encoder) {
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
        log.debug("Decoded URL: {}", decodedUrl);
        return ResponseEntity.ok(decodedUrl);
    }
}
