package com.strategyobject.substrateclient.storage;

import java.util.concurrent.CompletableFuture;

public interface MultiQuery<V> {
    CompletableFuture<KeyValueCollection<V>> execute();
}
