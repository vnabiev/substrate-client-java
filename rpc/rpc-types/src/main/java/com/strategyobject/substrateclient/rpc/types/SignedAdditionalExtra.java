package com.strategyobject.substrateclient.rpc.types;

import com.strategyobject.substrateclient.scale.ScaleType;
import com.strategyobject.substrateclient.scale.annotations.Scale;
import com.strategyobject.substrateclient.scale.annotations.ScaleWriter;
import lombok.Getter;
import lombok.NonNull;

@Getter
@ScaleWriter
public class SignedAdditionalExtra implements AdditionalExtra {
    @Scale(ScaleType.U32.class)
    private final long specVersion;
    @Scale(ScaleType.U32.class)
    private final long txVersion;
    private final BlockHash genesis;
    private final BlockHash eraBlock;

    SignedAdditionalExtra(long specVersion, long txVersion, @NonNull BlockHash genesis, @NonNull BlockHash eraBlock) {
        this.specVersion = (int) specVersion;
        this.txVersion = (int) txVersion;
        this.genesis = genesis;
        this.eraBlock = eraBlock;
    }
}
