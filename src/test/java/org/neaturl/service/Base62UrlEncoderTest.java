package org.neaturl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.neaturl.service.repository.base62.Base62Url;
import org.neaturl.service.repository.base62.Base62UrlRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires complets pour Base62UrlEncoder
 */
class Base62UrlEncoderTest {

    private Base62UrlRepository repo;
    private Base62UrlEncoder encoder;

    @BeforeEach
    void setup() {
        repo = mock(Base62UrlRepository.class);
        encoder = new Base62UrlEncoder(repo);
    }

    // ------------------------------------------------------
    // ENCODE
    // ------------------------------------------------------

    @Test
    @DisplayName("encode() doit retourner un caractère 'a' quand id == 0")
    void encode_shouldReturnAWhenIdIsZero() {
        Base62Url entity = new Base62Url("https://example.com");
        entity.setId(0L);

        when(repo.save(any())).thenReturn(entity);

        String result = encoder.encode("https://example.com");

        assertEquals("a", result);
        verify(repo).save(any(Base62Url.class));
    }

    @Test
    @DisplayName("encode() doit convertir correctement un id en Base62")
    void encode_shouldConvertIdToBase62() {
        Base62Url entity = new Base62Url("https://example.com");
        entity.setId(125L); // devrait donner "cb"
        when(repo.save(any())).thenReturn(entity);

        String result = encoder.encode("https://example.com");

        assertEquals("cb", result);
    }

    @Test
    @DisplayName("encode() doit sauvegarder l'URL avant d'encoder l'id")
    void encode_shouldSaveEntity() {
        Base62Url entity = new Base62Url("https://unit.test");
        entity.setId(5L);
        when(repo.save(any())).thenReturn(entity);

        encoder.encode("https://unit.test");

        ArgumentCaptor<Base62Url> captor = ArgumentCaptor.forClass(Base62Url.class);
        verify(repo).save(captor.capture());
        assertEquals("https://unit.test", captor.getValue().getUrl());
    }

    // ------------------------------------------------------
    // DECODE
    // ------------------------------------------------------

    @Test
    @DisplayName("decode() doit retourner l'URL originale pour un code valide")
    void decode_shouldReturnOriginalUrl() {
        Base62Url entity = new Base62Url("https://decode.test");
        entity.setId(125L);
        when(repo.findById(125L)).thenReturn(Optional.of(entity));

        Optional<String> result = encoder.decode("cb");

        assertTrue(result.isPresent());
        assertEquals("https://decode.test", result.get());
    }

    @Test
    @DisplayName("decode() doit retourner Optional.empty() quand l'id n'existe pas")
    void decode_shouldReturnEmptyWhenNotFound() {
        when(repo.findById(anyLong())).thenReturn(Optional.empty());

        Optional<String> result = encoder.decode("cb");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("decode() doit lever InvalidEncodedUrl pour un caractère invalide")
    void decode_shouldThrowWhenInvalidCharacter() {
        assertThrows(InvalidEncodedUrl.class, () -> encoder.decode("c$"));
    }

    // ------------------------------------------------------
    // COHÉRENCE ENCODE / DECODE
    // ------------------------------------------------------

    @Test
    @DisplayName("encode() et decode() doivent être cohérents pour un même id")
    void encodeDecode_shouldBeConsistent() {
        // simulate repo.save()
        Base62Url saved = new Base62Url("https://neaturl.com/test");
        saved.setId(999L);
        when(repo.save(any())).thenReturn(saved);

        String encoded = encoder.encode("https://neaturl.com/test");
        assertNotNull(encoded);

        // simulate repo.findById()
        when(repo.findById(saved.getId()))
                .thenReturn(Optional.of(saved));

        Optional<String> decoded = encoder.decode(encoded);
        assertTrue(decoded.isPresent());
        assertEquals("https://neaturl.com/test", decoded.get());
    }
}
