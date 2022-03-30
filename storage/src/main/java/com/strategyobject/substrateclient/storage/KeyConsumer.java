package com.strategyobject.substrateclient.storage;

import lombok.NonNull;

import java.util.List;

@FunctionalInterface
public interface KeyConsumer {
    void accept(@NonNull List<Object> keys);
}
