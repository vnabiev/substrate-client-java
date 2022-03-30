package com.strategyobject.substrateclient.storage;

import lombok.NonNull;
import lombok.val;
import lombok.var;
import org.bouncycastle.crypto.digests.Blake2bDigest;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

public class Blake2B128Concat implements KeyHashingAlgorithm {
    private static final int BLAKE_128_HASH_SIZE = 16;
    private static volatile Blake2B128Concat instance;

    private static byte[] blake2_128(byte[] value) {
        val digest = new Blake2bDigest(128);
        digest.update(value, 0, value.length);

        val result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return result;
    }

    public static Blake2B128Concat getInstance() {
        if (instance == null) {
            synchronized (Blake2B128Concat.class) {
                if (instance == null) {
                    instance = new Blake2B128Concat();
                }
            }
        }
        return instance;
    }

    @Override
    public byte[] getHash(byte @NonNull [] encodedKey) {
        return ByteBuffer.allocate(BLAKE_128_HASH_SIZE + encodedKey.length)
                .put(blake2_128(encodedKey))
                .put(encodedKey)
                .array();
    }

    @Override
    public int hashSize() {
        return BLAKE_128_HASH_SIZE;
    }

    public byte[] deriveKey(byte @NonNull [] suffix, @NonNull Consumer<byte[]> consumer) {
        val hash = Arrays.copyOfRange(suffix, 0, BLAKE_128_HASH_SIZE);
        var key = new byte[0];
        var keyLength = 0;

        while (!Arrays.equals(hash, blake2_128(key))) {
            keyLength++;
            key = Arrays.copyOfRange(suffix, BLAKE_128_HASH_SIZE, BLAKE_128_HASH_SIZE + keyLength);
        }

        consumer.accept(key);

        return Arrays.copyOfRange(suffix, BLAKE_128_HASH_SIZE + keyLength, suffix.length);
    }
}
