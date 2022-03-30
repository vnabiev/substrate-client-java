package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.scale.ScaleReader;
import com.strategyobject.substrateclient.scale.ScaleWriter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor(staticName = "with")
public class KeyHasher<T> {
    private final @NonNull ScaleWriter<T> keyWriter;
    private final @NonNull ScaleReader<T> keyReader;
    private final @NonNull KeyHashingAlgorithm algorithm;

    public byte[] getHash(T key) throws IOException {
        val buf = new ByteArrayOutputStream();
        keyWriter.write(key, buf);

        return algorithm.getHash(buf.toByteArray());
    }

    public T extractKey(@NonNull InputStream storageKeySuffix) throws IOException {
        val skip = storageKeySuffix.skip(algorithm.hashSize());
        if (skip != algorithm.hashSize()) {
            throw new RuntimeException("Stream couldn't skip the hash.");
        }

        return keyReader.read(storageKeySuffix);
    }
}
