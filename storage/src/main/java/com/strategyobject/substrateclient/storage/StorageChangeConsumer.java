package com.strategyobject.substrateclient.storage;

import com.strategyobject.substrateclient.rpc.types.BlockHash;

import java.util.List;

public interface StorageChangeConsumer<V> {
    void accept(Exception exception, BlockHash block, V value, List<Object> keys);
}
