package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.rpc.types.BlockHash;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

public interface StorageDoubleMap<F, S, V> {
    CompletableFuture<V> get(@NonNull F first, @NonNull S second);

    CompletableFuture<V> at(@NonNull BlockHash block, @NonNull F first, @NonNull S second);
}
