package org.neaturl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neaturl.service.repository.base62.Base62Url;
import org.neaturl.service.repository.base62.Base62UrlRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Base62UrlEncoderTest {

    public static final long URL_ID = 125L;
    public static final String URL_TO_ENCODE = "https://example.com";
    public static final String URN_TO_DECODE = "https://decode.test";

    @Mock
    private Base62UrlRepository repo;
    private Base62UrlEncoder encoder;

    @BeforeEach
    void setup() {
        encoder = new Base62UrlEncoder(repo);
    }

    // ------------------------------------------------------
    // ENCODE
    // ------------------------------------------------------

    @Test
    void encode_shouldReturnAWhenIdIsZero() {
        var entity = new Base62Url(URL_TO_ENCODE);
        entity.setId(0L);

        when(repo.findByUrl(URL_TO_ENCODE)).thenReturn(Optional.empty());
        when(repo.save(any())).thenReturn(entity);

        var result = encoder.encode(URL_TO_ENCODE);

        assertEquals("a", result);
        verify(repo).save(any(Base62Url.class));
    }

    @Test
    void encode_shouldConvertIdToBase62() {
        var entity = new Base62Url(URL_TO_ENCODE);
        entity.setId(URL_ID);

        when(repo.findByUrl(URL_TO_ENCODE)).thenReturn(Optional.empty());
        when(repo.save(any())).thenReturn(entity);
        when(repo.findById(URL_ID)).thenReturn(Optional.of(entity));

        var result = encoder.encode(URL_TO_ENCODE);

        assertEquals("cb", result);
    }

    @Test
    void encode_shouldSaveEntity() {
        var entity = new Base62Url(URL_TO_ENCODE);
        entity.setId(URL_ID);

        when(repo.findByUrl(URL_TO_ENCODE)).thenReturn(Optional.empty());
        when(repo.save(any())).thenReturn(entity);
        when(repo.findById(URL_ID)).thenReturn(Optional.of(entity));

        encoder.encode(URL_TO_ENCODE);

        var captor = ArgumentCaptor.forClass(Base62Url.class);
        verify(repo).save(captor.capture());
        assertEquals(URL_TO_ENCODE, captor.getValue().getUrl());
    }

    @Test
    void encode_shouldConvertToSameValue() {
        var entity = new Base62Url(URL_TO_ENCODE);
        entity.setId(URL_ID);

        when(repo.findByUrl(URL_TO_ENCODE))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(entity));
        when(repo.save(any())).thenReturn(entity);
        when(repo.findById(URL_ID)).thenReturn(Optional.of(entity));

        var result = encoder.encode(URL_TO_ENCODE);
        var expectedValue = "cb";
        assertEquals(expectedValue, result);

        result = encoder.encode(URL_TO_ENCODE);
        assertEquals(expectedValue, result);
    }

    @Test
    void encode_shouldConvertToDifferentValue() {
        var entity = new Base62Url(URL_TO_ENCODE);
        entity.setId(URL_ID);

        when(repo.findByUrl(URL_TO_ENCODE)).thenReturn(Optional.empty());
        when(repo.save(any())).thenReturn(entity);
        var anotherEntity = new Base62Url("https://anotherurl.com");
        anotherEntity.setId(URL_ID);
        when(repo.findById(URL_ID)).thenReturn(Optional.of(anotherEntity));

        assertThrows(EncodingException.class,
                () -> encoder.encode(URL_TO_ENCODE),
                "The decoded URL is not identical to the original one. Decoded URL: " + anotherEntity.getUrl());
    }

    @Test
    void encode_encodedUrlShouldNotBeFound() {
        var entity = new Base62Url(URL_TO_ENCODE);
        entity.setId(URL_ID);

        when(repo.findByUrl(URL_TO_ENCODE)).thenReturn(Optional.empty());
        when(repo.save(any())).thenReturn(entity);
        when(repo.findById(URL_ID)).thenReturn(Optional.empty());

        assertThrows(EncodingException.class,
                () -> encoder.encode(URL_TO_ENCODE),
                "Encoded URL not found for " + URL_TO_ENCODE);
    }

    // ------------------------------------------------------
    // DECODE
    // ------------------------------------------------------

    @Test
    void decode_shouldReturnOriginalUrl() {
        var entity = new Base62Url(URN_TO_DECODE);
        entity.setId(URL_ID);

        when(repo.findById(URL_ID)).thenReturn(Optional.of(entity));

        var result = encoder.decode("cb");

        assertTrue(result.isPresent());
        assertEquals(URN_TO_DECODE, result.get());
    }

    @Test
    void decode_shouldReturnEmptyWhenNotFound() {
        when(repo.findById(anyLong())).thenReturn(Optional.empty());

        var result = encoder.decode("cb");

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
        var saved = new Base62Url(URN_TO_DECODE);
        saved.setId(URL_ID);

        when(repo.save(any())).thenReturn(saved);
        when(repo.findById(saved.getId()))
                .thenReturn(Optional.of(saved));

        var encoded = encoder.encode(URN_TO_DECODE);
        assertNotNull(encoded);

        var decoded = encoder.decode(encoded);
        assertTrue(decoded.isPresent());
        assertEquals(URN_TO_DECODE, decoded.get());
    }
}
