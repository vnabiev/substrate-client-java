package com.strategyobject.substrateclient.storage;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "sizeOf")
public class Identity implements KeyHashingAlgorithm {
    private final int size;

    @Override
    public byte[] getHash(byte @NonNull [] encodedKey) {
        return encodedKey;
    }

    @Override
    public int hashSize() {
        return 0;
    }
}
