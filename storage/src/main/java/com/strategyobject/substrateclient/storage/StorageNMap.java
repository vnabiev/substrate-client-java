package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.rpc.types.BlockHash;
import com.strategyobject.substrateclient.rpc.types.Hash;
import com.strategyobject.substrateclient.types.tuples.Pair;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface StorageNMap<V> {
    CompletableFuture<V> get(@NonNull Object... keys);

    CompletableFuture<V> at(@NonNull BlockHash block, @NonNull Object... keys);

    CompletableFuture<Hash> hash(@NonNull Object... keys);

    CompletableFuture<Hash> hashAt(@NonNull BlockHash block, @NonNull Object... keys);

    CompletableFuture<Long> size(@NonNull Object... keys);

    CompletableFuture<Long> sizeAt(@NonNull BlockHash block, @NonNull Object... keys);

    CompletableFuture<KeyCollection<V>> keys(@NonNull Object... keys);

    CompletableFuture<KeyCollection<V>> keysAt(@NonNull BlockHash block, @NonNull Object... keys);

    PagedKeyCollection<V> keysPaged(int count, @NonNull Object... keys);

    QueryableKey<V> query(@NonNull Object... keys);

    CompletableFuture<List<Pair<BlockHash, List<V>>>> history(@NonNull Object... keys);

    CompletableFuture<KeyValueCollection<V>> entries(@NonNull Object... keys);

    PagedKeyValueCollection<V> entriesPaged(int count, @NonNull Object... keys);

    CompletableFuture<KeyValueCollection<V>> multi(@NonNull Arg... args);

    CompletableFuture<Supplier<CompletableFuture<Boolean>>> subscribe(@NonNull StorageChangeConsumer<V> consumer,
                                                                      @NonNull Arg... args);
}
