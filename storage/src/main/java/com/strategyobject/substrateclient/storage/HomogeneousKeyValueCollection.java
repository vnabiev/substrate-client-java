package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.rpc.types.StorageData;
import com.strategyobject.substrateclient.rpc.types.StorageKey;
import com.strategyobject.substrateclient.scale.ScaleReader;
import com.strategyobject.substrateclient.types.tuples.Pair;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

@RequiredArgsConstructor(staticName = "with")
class HomogeneousKeyValueCollection<V> implements KeyValueCollection<V> {
    private final @NonNull List<Pair<StorageKey, StorageData>> pairs;
    private final @NonNull ScaleReader<V> valueReader;
    private final @NonNull Function<StorageKey, List<Object>> keyExtractor;

    @Override
    public Iterator<Entry<V>> iterator() {
        return new Iterator<Entry<V>>() {
            private final Iterator<Pair<StorageKey, StorageData>> underlying = pairs.iterator();

            @Override
            public boolean hasNext() {
                return underlying.hasNext();
            }

            @Override
            public Entry<V> next() {
                if (hasNext()) {
                    val pair = underlying.next();

                    return consumer -> {
                        try {
                            val value = pair.getValue1() == null ?
                                    null :
                                    valueReader.read(new ByteArrayInputStream(pair.getValue1().getData()));
                            val keys = keyExtractor.apply(pair.getValue0());

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
