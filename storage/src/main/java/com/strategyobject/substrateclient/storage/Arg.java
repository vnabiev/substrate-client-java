package com.strategyobject.substrateclient.storage;

import lombok.Getter;
import lombok.NonNull;

public class Arg {
    @Getter
    private final Object[] list;

    private Arg(Object[] list) {
        this.list = list;
    }

    public static Arg of(@NonNull Object... keys) {
        return new Arg(keys);
    }
}
