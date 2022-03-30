package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.rpc.types.StorageKey;
import com.strategyobject.substrateclient.scale.ScaleReader;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface QueryableKey<V> {
    CompletableFuture<V> execute();

    StorageKey getKey();

    ScaleReader<V> getValueReader();

    void consume(@NonNull KeyConsumer consumer);

    MultiQuery<Object> join(@NonNull QueryableKey<?>... others);

    List<Object> extractKeys(@NonNull StorageKey fullKey);
}
