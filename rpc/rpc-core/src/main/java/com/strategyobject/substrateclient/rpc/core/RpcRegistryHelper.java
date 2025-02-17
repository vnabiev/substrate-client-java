package com.strategyobject.substrateclient.rpc.core;

import com.strategyobject.substrateclient.rpc.core.registries.RpcDecoderRegistry;
import com.strategyobject.substrateclient.rpc.core.registries.RpcEncoderRegistry;
import lombok.val;

public final class RpcRegistryHelper {
    private RpcRegistryHelper() {
    }

    @SuppressWarnings("unchecked")
    public static <T> RpcDecoder<T> resolveAndInjectOrNull(Class<T> clazz, DecoderPair<?>... dependencies) {
        val target = (RpcDecoder<T>) RpcDecoderRegistry.getInstance().resolve(clazz);

        if (target == null) {
            return null;
        }

        return target.inject(dependencies);
    }

    @SuppressWarnings("unchecked")
    public static <T> RpcEncoder<T> resolveAndInjectOrNull(Class<T> clazz, EncoderPair<?>... dependencies) {
        val target = (RpcEncoder<T>) RpcEncoderRegistry.getInstance().resolve(clazz);

        if (target == null) {
            return null;
        }

        return target.inject(dependencies);
    }
}
