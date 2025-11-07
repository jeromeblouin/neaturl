package org.neaturl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.neaturl.service.repository.base62.Base62Url;
import org.neaturl.service.repository.base62.Base62UrlRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void encode_shouldReturnAWhenIdIsZero() {
        Base62Url entity = new Base62Url("https://example.com");
        entity.setId(0L);

        when(repo.save(any())).thenReturn(entity);

        String result = encoder.encode("https://example.com");

        assertEquals("a", result);
        verify(repo).save(any(Base62Url.class));
    }

    @Test
    void encode_shouldConvertIdToBase62() {
        Base62Url entity = new Base62Url("https://example.com");
        entity.setId(125L); // devrait donner "cb"
        when(repo.save(any())).thenReturn(entity);

        String result = encoder.encode("https://example.com");

        assertEquals("cb", result);
    }

    @Test
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
    void decode_shouldReturnOriginalUrl() {
        Base62Url entity = new Base62Url("https://decode.test");
        entity.setId(125L);
        when(repo.findById(125L)).thenReturn(Optional.of(entity));

        Optional<String> result = encoder.decode("cb");

        assertTrue(result.isPresent());
        assertEquals("https://decode.test", result.get());
    }

    @Test
    void decode_shouldReturnEmptyWhenNotFound() {
        when(repo.findById(anyLong())).thenReturn(Optional.empty());

        Optional<String> result = encoder.decode("cb");

        assertTrue(result.isEmpty());
    }

    @Test
    void decode_shouldThrowWhenInvalidCharacter() {
        assertThrows(EncodingException.class, () -> encoder.decode("c$"));
    }

    // ------------------------------------------------------
    // COHÉRENCE ENCODE / DECODE
    // ------------------------------------------------------

    @Test
    void encodeDecode_shouldBeConsistent() {
        Base62Url saved = new Base62Url("https://neaturl.com/test");
        saved.setId(999L);
        when(repo.save(any())).thenReturn(saved);
        when(repo.findById(saved.getId()))
                .thenReturn(Optional.of(saved));

        String encoded = encoder.encode("https://neaturl.com/test");
        assertNotNull(encoded);

        Optional<String> decoded = encoder.decode(encoded);
        assertTrue(decoded.isPresent());
        assertEquals("https://neaturl.com/test", decoded.get());
    }
}
