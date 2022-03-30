package com.strategyobject.substrateclient.storage;

import java.util.concurrent.CompletableFuture;

public interface PagedKeyCollection<V> {
    int number();

    CompletableFuture<Boolean> moveNext();

    KeyCollection<V> current();
}
