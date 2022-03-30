package com.strategyobject.substrateclient.storage;

import java.util.Iterator;

public interface KeyValueCollection<V> {
    Iterator<Entry<V>> iterator();
}
