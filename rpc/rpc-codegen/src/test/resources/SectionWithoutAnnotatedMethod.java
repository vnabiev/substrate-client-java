package com.strategyobject.substrateclient.rpc.sections;

import com.strategyobject.substrateclient.rpc.core.annotations.RpcInterface;

import java.util.concurrent.CompletableFuture;

@RpcInterface("empty")
public interface SectionWithoutAnnotatedMethod {
    CompletableFuture<Boolean> doNothing();
}
