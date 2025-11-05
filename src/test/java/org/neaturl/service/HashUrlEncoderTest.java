package org.neaturl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.neaturl.service.repository.hashedurl.HashedUrl;
import org.neaturl.service.repository.hashedurl.HashedUrlRepository;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HashUrlEncoderTest {

    private HashedUrlRepository repo;
    private Random random;
    private HashUrlEncoder encoder;

    @BeforeEach
    void setup() {
        repo = mock(HashedUrlRepository.class);
        random = mock(Random.class);
        encoder = new HashUrlEncoder(repo, random);
    }

    // ------------------------------------------------------------
    // ENCODE
    // ------------------------------------------------------------

    @Test
    void encode_shouldCreate8CharHash() {
        when(repo.findById(anyString())).thenReturn(Optional.empty());
        var url = "https://example.com";

        String hash = encoder.encode(url);

        assertNotNull(hash);
        assertEquals(8, hash.length(), "Le hash doit avoir une longueur fixe de 8");
        verify(repo).save(any(HashedUrl.class));
    }

    @Test
    void encode_shouldCheckForExistingHash() {
        when(repo.findById(anyString()))
                .thenReturn(Optional.empty());

        encoder.encode("https://unit.test");

        verify(repo, atLeastOnce()).findById(anyString());
    }

    @Test
    void encode_shouldHandleCollision() {
        // Première tentative : collision
        when(repo.findById(anyString()))
                .thenReturn(Optional.of(new HashedUrl("abcdef12", "old")))
                .thenReturn(Optional.empty());

        when(random.nextInt(anyInt())).thenReturn(5); // simulate deterministic random char

        String hash = encoder.encode("https://collision.test");

        assertNotNull(hash);
        assertEquals(8, hash.length());
        verify(repo, times(2)).findById(anyString());
        verify(repo).save(any(HashedUrl.class));
    }

    @Test
    void encode_shouldSaveEntityWithCorrectValues() {
        when(repo.findById(anyString())).thenReturn(Optional.empty());
        var url = "https://neaturl.dev";

        String hash = encoder.encode(url);

        ArgumentCaptor<HashedUrl> captor = ArgumentCaptor.forClass(HashedUrl.class);
        verify(repo).save(captor.capture());
        HashedUrl saved = captor.getValue();

        assertEquals(url, saved.getUrl());
        assertEquals(hash, saved.getId());
    }

    // ------------------------------------------------------------
    // DECODE
    // ------------------------------------------------------------

    @Test
    void decode_shouldReturnOriginalUrl() {
        when(repo.findById("abcdef12"))
                .thenReturn(Optional.of(new HashedUrl("abcdef12", "https://found.test")));

        Optional<String> result = encoder.decode("abcdef12");

        assertTrue(result.isPresent());
        assertEquals("https://found.test", result.get());
    }

    @Test
    void decode_shouldReturnEmptyWhenNotFound() {
        when(repo.findById("notfound")).thenReturn(Optional.empty());

        Optional<String> result = encoder.decode("notfound");

        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------
    // COHÉRENCE GLOBALE
    // ------------------------------------------------------------

    @Test
    void encodeDecode_shouldBeConsistent() {
        when(repo.findById(anyString()))
                .thenReturn(Optional.empty());

        String hash = encoder.encode("https://neaturl.com/test");

        when(repo.findById(hash))
                .thenReturn(Optional.of(new HashedUrl(hash, "https://neaturl.com/test")));

        Optional<String> decoded = encoder.decode(hash);

        assertTrue(decoded.isPresent());
        assertEquals("https://neaturl.com/test", decoded.get());
    }
}
