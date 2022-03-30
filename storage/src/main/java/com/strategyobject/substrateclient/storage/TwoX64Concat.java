package com.strategyobject.substrateclient.storage;

import lombok.NonNull;
import lombok.val;
import net.openhft.hashing.LongHashFunction;

import java.nio.ByteBuffer;

public class TwoX64Concat implements KeyHashingAlgorithm {
    private static final int XX_HASH_SIZE = 8;
    private static volatile TwoX64Concat instance;

    private static byte[] xxHash64(byte[] value) {
        val hash = LongHashFunction.xx(0).hashBytes(value);

        return new byte[]{
                (byte) ((hash) & 0xff),
                (byte) ((hash >> 8) & 0xff),
                (byte) ((hash >> 16) & 0xff),
                (byte) ((hash >> 24) & 0xff),
                (byte) ((hash >> 32) & 0xff),
                (byte) ((hash >> 40) & 0xff),
                (byte) ((hash >> 48) & 0xff),
                (byte) ((hash >> 56) & 0xff)
        };
    }

    private static byte[] xxHash64Concat(byte[] value) {
        val buf = ByteBuffer.allocate(XX_HASH_SIZE + value.length);

        buf.put(xxHash64(value));
        buf.put(value);

        return buf.array();
    }

    public static TwoX64Concat getInstance() {
        if (instance == null) {
            synchronized (TwoX64Concat.class) {
                if (instance == null) {
                    instance = new TwoX64Concat();
                }
            }
        }
        return instance;
    }

    @Override
    public byte[] getHash(byte @NonNull [] encodedKey) {
        return xxHash64Concat(encodedKey);
    }

    @Override
    public int hashSize() {
        return XX_HASH_SIZE;
    }

}
