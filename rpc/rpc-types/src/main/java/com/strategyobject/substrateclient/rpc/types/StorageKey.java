package com.strategyobject.substrateclient.rpc.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "valueOf")
public class StorageKey {
    private final byte[] data;
}
