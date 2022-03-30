package com.strategyobject.substrateclient.storage;

import lombok.NonNull;

public interface KeyHashingAlgorithm {
    byte[] getHash(byte @NonNull [] encodedKey);

    int hashSize();
}
