package org.neaturl.api;

import org.neaturl.service.UrlEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EncoderController {

    private final UrlEncoder encoder;

    public EncoderController(UrlEncoder encoder) {
        this.encoder = encoder;
    }

    @GetMapping("encode")
    public ResponseEntity<String> encode(String url) {
        return ResponseEntity.ok(encoder.encode(url));
    }
}
