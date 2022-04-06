package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.rpc.types.BlockHash;
import com.strategyobject.substrateclient.types.tuples.Pair;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StorageValue<V> {
    CompletableFuture<V> get();

    CompletableFuture<V> at(@NonNull BlockHash block);

    CompletableFuture<List<Pair<BlockHash, List<V>>>> history();

    QueryableKey<V> query();
}
