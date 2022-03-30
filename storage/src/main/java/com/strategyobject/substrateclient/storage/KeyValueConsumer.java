package com.strategyobject.substrateclient.storage;

import lombok.NonNull;

import java.util.List;

public interface KeyValueConsumer<V> {
    void accept(V value, @NonNull List<Object> keys);
}
