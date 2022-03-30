package com.strategyobject.substrateclient.storage;

public interface Entry<V> {
    void consume(KeyValueConsumer<V> consumer);
}
