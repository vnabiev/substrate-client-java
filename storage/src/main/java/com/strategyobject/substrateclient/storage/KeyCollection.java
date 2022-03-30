package com.strategyobject.substrateclient.storage;

import java.util.Iterator;

public interface KeyCollection<V> {
    int size();

    MultiQuery<V> multi();

    Iterator<QueryableKey<V>> iterator();
}
