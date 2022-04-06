package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.rpc.types.StorageKey;
import lombok.*;
import net.openhft.hashing.LongHashFunction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StorageKeyProvider {
    private static final int XX_HASH_SIZE = 16;
    @Getter(AccessLevel.PACKAGE)
    private final List<KeyHasher<?>> keyHashers = new ArrayList<>();
    private final ByteBuffer keyPrefix;

    private StorageKeyProvider(String palletName, String storageName) {
        keyPrefix = ByteBuffer.allocate(XX_HASH_SIZE * 2);

        xxHash128(keyPrefix, palletName);
        xxHash128(keyPrefix, storageName);
    }

    private static void xxHash128(ByteBuffer buf, String value) {
        val encodedValue = value.getBytes(StandardCharsets.UTF_8);

        final ByteOrder sourceOrder = buf.order(); // final ByteOrder instead of val because of checkstyle
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.asLongBuffer()
                .put(LongHashFunction.xx(0).hashBytes(encodedValue))
                .put(LongHashFunction.xx(1).hashBytes(encodedValue));

        buf.position(buf.position() + XX_HASH_SIZE);
        buf.order(sourceOrder);
    }

    public static StorageKeyProvider with(@NonNull String palletName, @NonNull String storageName) {
        return new StorageKeyProvider(palletName, storageName);
    }

    public StorageKeyProvider with(@NonNull KeyHasher<?>... hashers) {
        if (keyHashers.size() > 0) {
            throw new IllegalStateException("Key hashers are already set.");
        }

        Collections.addAll(keyHashers, hashers);

        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public StorageKey get(@NonNull Object... keys) {
        if (keys.length > keyHashers.size()) {
            throw new IndexOutOfBoundsException(
                    String.format("Number of keys mustn't exceed capacity. Passed: %s, capacity: %s.",
                            keys.length,
                            keyHashers.size()));
        }

        val stream = new ByteArrayOutputStream();
        try {
            stream.write(keyPrefix.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (var i = 0; i < keys.length; i++) {
            try {
                stream.write(((KeyHasher) keyHashers.get(i)).getHash(keys[i]));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return StorageKey.valueOf(stream.toByteArray());
    }

    public int countOfKeys() {
        return keyHashers.size();
    }

    public List<Object> extractKeys(StorageKey fullKey) {
        val stream = new ByteArrayInputStream(Arrays.copyOfRange(fullKey.getData(),
                keyPrefix.capacity(),
                fullKey.getData().length));

        return keyHashers
                .stream()
                .map(keyHasher -> {
                    try {
                        return keyHasher.extractKey(stream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Object> extractKeys(StorageKey fullKey, StorageKey queryKey, int countOfKeysInQuery) {
        val stream = new ByteArrayInputStream(Arrays.copyOfRange(fullKey.getData(),
                queryKey.getData().length,
                fullKey.getData().length));

        return keyHashers
                .stream()
                .skip(countOfKeysInQuery)
                .map(keyHasher -> {
                    try {
                        return keyHasher.extractKey(stream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

}
