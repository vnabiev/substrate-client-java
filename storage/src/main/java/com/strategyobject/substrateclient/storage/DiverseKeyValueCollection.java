package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.rpc.types.StorageData;
import com.strategyobject.substrateclient.rpc.types.StorageKey;
import com.strategyobject.substrateclient.scale.ScaleReader;
import com.strategyobject.substrateclient.types.tuples.Pair;
import lombok.NonNull;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class DiverseKeyValueCollection implements KeyValueCollection<Object> {
    private final List<ScaleReader<Object>> valueReaders;
    private final List<Pair<StorageKey, StorageData>> pairs;
    private final List<Function<StorageKey, List<Object>>> keyExtractors;

    private DiverseKeyValueCollection(List<Pair<StorageKey, StorageData>> pairs,
                                      List<ScaleReader<Object>> valueReaders,
                                      List<Function<StorageKey, List<Object>>> keyExtractors) {
        if (pairs.size() != valueReaders.size()) {
            throw new IllegalArgumentException("Number of value readers doesn't match number of pairs.");
        }

        if (pairs.size() != keyExtractors.size()) {
            throw new IllegalArgumentException("Number of extractors doesn't match number of pairs.");
        }

        this.valueReaders = valueReaders;
        this.pairs = pairs;
        this.keyExtractors = keyExtractors;
    }

    public static KeyValueCollection<Object> with(@NonNull List<Pair<StorageKey, StorageData>> pairs,
                                                  List<ScaleReader<Object>> valueReaders,
                                                  @NonNull List<Function<StorageKey, List<Object>>> keyExtractors) {
        return new DiverseKeyValueCollection(pairs, valueReaders, keyExtractors);
    }

    @Override
    public Iterator<Entry<Object>> iterator() {
        return new Iterator<Entry<Object>>() {
            private final Iterator<Pair<StorageKey, StorageData>> underlying = pairs.iterator();
            private final Iterator<ScaleReader<Object>> readers = valueReaders.iterator();
            private final Iterator<Function<StorageKey, List<Object>>> extractors = keyExtractors.iterator();

            @Override
            public boolean hasNext() {
                return underlying.hasNext();
            }

            @Override
            public Entry<Object> next() {
                if (hasNext()) {
                    val pair = underlying.next();

                    return consumer -> {
                        try {
                            val value = pair.getValue1() == null ?
                                    null :
                                    readers.next().read(new ByteArrayInputStream(pair.getValue1().getData()));
                            val keys = extractors.next().apply(pair.getValue0());

                            consumer.accept(value, keys);
                        } catch (IOException e) {
                            throw new RuntimeException();
                        }
                    };
                }

                throw new NoSuchElementException();
            }
        };
    }
}
