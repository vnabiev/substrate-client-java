package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.rpc.Rpc;
import com.strategyobject.substrateclient.rpc.types.BlockHash;
import com.strategyobject.substrateclient.rpc.types.Hash;
import com.strategyobject.substrateclient.rpc.types.StorageKey;
import com.strategyobject.substrateclient.scale.ScaleReader;
import com.strategyobject.substrateclient.types.tuples.Pair;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "with")
public class StorageNMapImpl<V> implements StorageNMap<V> {
    private final @NonNull Rpc rpc;
    private final @NonNull ScaleReader<V> scaleReader;
    private final @NonNull StorageKeyProvider storageKeyProvider;

    @Override
    public CompletableFuture<V> get(@NonNull Object... keys) {
        ensureAllKeysWerePassed(keys);

        return rpc
                .getState()
                .getStorage(storageKeyProvider.get(keys))
                .thenApplyAsync(d -> {
                    if (d == null) {
                        return null;
                    }

                    try {
                        return scaleReader.read(new ByteArrayInputStream(d.getData()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void ensureAllKeysWerePassed(@NonNull Object[] keys) {
        if (keys.length != storageKeyProvider.countOfKeys()) {
            throw new IndexOutOfBoundsException(String.format("Incorrect number of keys were passed. Passed: %s, expected: %s.",
                    keys.length,
                    storageKeyProvider.countOfKeys()));
        }
    }

    @Override
    public CompletableFuture<V> at(@NonNull BlockHash block, @NonNull Object... keys) {
        ensureAllKeysWerePassed(keys);

        return rpc
                .getState()
                .getStorage(storageKeyProvider.get(keys), block)
                .thenApplyAsync(d -> {
                    if (d == null) {
                        return null;
                    }

                    try {
                        return scaleReader.read(new ByteArrayInputStream(d.getData()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<Hash> hash(@NonNull Object... keys) {
        ensureAllKeysWerePassed(keys);

        return rpc
                .getState()
                .getStorageHash(storageKeyProvider.get(keys));
    }

    @Override
    public CompletableFuture<Hash> hashAt(@NonNull BlockHash block, @NonNull Object... keys) {
        ensureAllKeysWerePassed(keys);

        return rpc
                .getState()
                .getStorageHash(storageKeyProvider.get(keys), block);
    }

    @Override
    public CompletableFuture<Long> size(@NonNull Object... keys) {
        ensureAllKeysWerePassed(keys);

        return rpc
                .getState()
                .getStorageSize(storageKeyProvider.get(keys));
    }

    @Override
    public CompletableFuture<Long> sizeAt(@NonNull BlockHash block, @NonNull Object... keys) {
        ensureAllKeysWerePassed(keys);

        return rpc
                .getState()
                .getStorageSize(storageKeyProvider.get(keys), block);
    }

    @Override
    public CompletableFuture<KeyCollection<V>> keys(@NonNull Object... keys) {
        val queryKey = storageKeyProvider.get(keys);
        return rpc
                .getState()
                .getKeys(queryKey)
                .thenApplyAsync(storageKeys -> KeyCollectionImpl.with(rpc,
                        storageKeys,
                        scaleReader,
                        key -> storageKeyProvider.extractKeys(key, queryKey, keys.length)));
    }

    @Override
    public CompletableFuture<KeyCollection<V>> keysAt(@NonNull BlockHash block, @NonNull Object... keys) {
        val queryKey = storageKeyProvider.get(keys);
        return rpc
                .getState()
                .getKeys(queryKey, block)
                .thenApplyAsync(storageKeys -> KeyCollectionImpl.with(rpc,
                        storageKeys,
                        scaleReader,
                        key -> storageKeyProvider.extractKeys(key, queryKey, keys.length)));
    }

    @Override
    public PagedKeyCollection<V> keysPaged(int count, @NonNull Object... keys) {
        if (count <= 0) {
            throw new IllegalArgumentException("Number of elements per page must be positive.");
        }

        val queryKey = storageKeyProvider.get(keys);

        return new PagedKeyCollection<V>() {
            private int pageNumber = 0;
            private int size = 0;
            private boolean hasNext = false;
            private StorageKey last;
            private KeyCollection<V> collection;

            @Override
            public int number() {
                if (!hasNext) {
                    throw new NoSuchElementException();
                }

                return pageNumber;
            }

            @Override
            public CompletableFuture<Boolean> moveNext() {
                val task = pageNumber == 0 ?
                        rpc.getState().getKeysPaged(queryKey, count) :
                        size == count ?
                                rpc.getState().getKeysPaged(queryKey, count, last) :
                                CompletableFuture.completedFuture(Collections.<StorageKey>emptyList());

                return task
                        .thenApplyAsync(storageKeys -> {
                            if ((size = storageKeys.size()) > 0) {
                                hasNext = true;
                                pageNumber++;
                                last = storageKeys.get(storageKeys.size() - 1);
                                collection = KeyCollectionImpl.with(rpc,
                                        storageKeys,
                                        scaleReader,
                                        key -> storageKeyProvider.extractKeys(key, queryKey, keys.length));
                            } else {
                                hasNext = false;
                            }

                            return hasNext;
                        });
            }

            @Override
            public KeyCollection<V> current() {
                if (!hasNext) {
                    throw new NoSuchElementException();
                }

                return collection;
            }
        };
    }

    @Override
    public QueryableKey<V> query(@NonNull Object... keys) {
        ensureAllKeysWerePassed(keys);

        return QueryableKeyImpl.with(rpc,
                storageKeyProvider.get(keys),
                scaleReader,
                storageKeyProvider::extractKeys);
    }

    @Override
    public CompletableFuture<List<Pair<BlockHash, List<V>>>> history(@NonNull Object... keys) {
        ensureAllKeysWerePassed(keys);

        return rpc
                .getState()
                .queryStorageAt(Collections.singletonList(storageKeyProvider.get(keys)))
                .thenApplyAsync(set -> set
                        .stream()
                        .map(s -> Pair.of(
                                s.getBlock(),
                                s
                                        .getChanges()
                                        .stream()
                                        .map(p -> Optional.of(p.getValue1())
                                                .map(v -> {
                                                    try {
                                                        return scaleReader.read(new ByteArrayInputStream(v.getData()));
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                })
                                                .orElse(null))
                                        .collect(Collectors.toList())))
                        .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<KeyValueCollection<V>> entries(@NonNull Object... keys) {
        return keys(keys).thenComposeAsync(k -> k.multi().execute());
    }

    @Override
    public PagedKeyValueCollection<V> entriesPaged(int count, @NonNull Object... keys) {
        return new PagedKeyValueCollection<V>() {
            private final PagedKeyCollection<V> underlying = keysPaged(count, keys);
            private KeyValueCollection<V> collection;

            @Override
            public int number() {
                if (collection == null) {
                    throw new NoSuchElementException();
                }

                return underlying.number();
            }

            @Override
            public CompletableFuture<Boolean> moveNext() {
                val values = new CompletableFuture<KeyValueCollection<V>>();
                underlying.moveNext()
                        .whenCompleteAsync((next, throwable) -> {
                            if (throwable != null) {
                                values.completeExceptionally(throwable);
                            } else {
                                if (next) {
                                    underlying.current().multi().execute()
                                            .whenCompleteAsync((keyValues, e) -> {
                                                if (e != null) {
                                                    values.completeExceptionally(e);
                                                } else {
                                                    values.complete(keyValues);
                                                }
                                            });
                                } else {
                                    values.complete(null);
                                }
                            }
                        });

                return values
                        .whenCompleteAsync((keyValues, throwable) -> {
                            if (throwable == null) {
                                collection = keyValues;
                            }
                        }).thenApplyAsync(Objects::nonNull);
            }

            @Override
            public KeyValueCollection<V> current() {
                if (collection == null) {
                    throw new NoSuchElementException();
                }

                return collection;
            }
        };
    }

    @Override
    public CompletableFuture<KeyValueCollection<V>> multi(@NonNull Arg... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Multi requests are not supported without arguments.");
        }

        val keys = Arrays.stream(args)
                .map(a -> {
                    ensureAllKeysWerePassed(a.getList());

                    return storageKeyProvider.get(a.getList());
                })
                .collect(Collectors.toList());

        return rpc
                .getState()
                .queryStorageAt(keys)
                .thenApplyAsync(set -> {
                    val pairs = set
                            .stream()
                            .flatMap(changeSet -> changeSet.getChanges().stream())
                            .collect(Collectors.toList());

                    return HomogeneousKeyValueCollection.with(pairs,
                            scaleReader,
                            storageKeyProvider::extractKeys);
                });
    }

    @Override
    public CompletableFuture<Supplier<CompletableFuture<Boolean>>> subscribe(@NonNull StorageChangeConsumer<V> consumer, @NonNull Arg... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Subscription can't be requested with no arguments.");
        }

        val queryKeys = Arrays.stream(args)
                .map(a -> {
                    ensureAllKeysWerePassed(a.getList());

                    return storageKeyProvider.get(a.getList());
                })
                .collect(Collectors.toList());

        return rpc
                .getState()
                .subscribeStorage(queryKeys, (e, changeSet) -> {
                    if (e != null) {
                        consumer.accept(e, null, null, null);
                    } else {
                        changeSet.getChanges().forEach(
                                p -> {
                                    try {
                                        val value = p.getValue1() == null ?
                                                null :
                                                scaleReader.read(new ByteArrayInputStream(p.getValue1().getData()));

                                        val keys = storageKeyProvider.extractKeys(p.getValue0());
                                        consumer.accept(null, changeSet.getBlock(), value, keys);
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                        );
                    }
                });
    }

}
