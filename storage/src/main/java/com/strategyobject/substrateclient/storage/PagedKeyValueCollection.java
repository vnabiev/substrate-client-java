package com.strategyobject.substrateclient.storage;

import java.util.concurrent.CompletableFuture;

public interface PagedKeyValueCollection<V> {
    int number();

    CompletableFuture<Boolean> moveNext();

    KeyValueCollection<V> current();
}
