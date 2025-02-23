package com.strategyobject.substrateclient.scale.codegen.writer;

import com.squareup.javapoet.CodeBlock;
import com.strategyobject.substrateclient.common.codegen.Constants;
import com.strategyobject.substrateclient.common.codegen.ProcessorContext;
import com.strategyobject.substrateclient.common.codegen.TypeTraverser;
import lombok.NonNull;
import lombok.var;

import javax.lang.model.type.*;
import java.util.Map;

import static com.strategyobject.substrateclient.scale.codegen.ScaleProcessorHelper.SCALE_SELF_WRITABLE;

public class WriterCompositor extends TypeTraverser<CodeBlock> {
    private final ProcessorContext context;
    private final Map<String, Integer> typeVarMap;
    private final TypeMirror selfWritable;
    private final String writerAccessor;
    private final String registryVarName;
    private final TypeMirror arrayType;


    private WriterCompositor(ProcessorContext context,
                             Map<String, Integer> typeVarMap,
                             String writerAccessor,
                             String registryVarName) {
        super(CodeBlock.class);

        this.context = context;
        this.typeVarMap = typeVarMap;
        this.selfWritable = context.erasure(context.getType(SCALE_SELF_WRITABLE));
        this.writerAccessor = writerAccessor;
        this.registryVarName = registryVarName;
        this.arrayType = context.erasure(context.getType(Constants.ARRAY_TYPE));
    }

    public static WriterCompositor forAnyType(@NonNull ProcessorContext context,
                                              @NonNull Map<String, Integer> typeVarMap,
                                              @NonNull String writerAccessor,
                                              @NonNull String registryVarName) {
        return new WriterCompositor(context, typeVarMap, writerAccessor, registryVarName);
    }

    public static WriterCompositor disallowOpenGeneric(@NonNull ProcessorContext context,
                                                       @NonNull String registryVarName) {
        return new WriterCompositor(context, null, null, registryVarName);
    }

    private CodeBlock getNonGenericCodeBlock(TypeMirror type, TypeMirror override) {
        return CodeBlock.builder()
                .add("$L.resolve($T.class)",
                        registryVarName,
                        override != null ?
                                override :
                                context.isAssignable(type, selfWritable) ?
                                        selfWritable :
                                        type)
                .build();
    }

    private CodeBlock getGenericCodeBlock(TypeMirror type, TypeMirror override, CodeBlock[] subtypes) {
        TypeMirror resolveType;
        if (override != null) {
            if (context.isNonGeneric(override)) {
                return CodeBlock.builder()
                        .add("$L.resolve($T.class)", registryVarName, override)
                        .build();
            }

            resolveType = override;
        } else {
            resolveType = context.erasure(type);

            if (context.isAssignable(resolveType, selfWritable)) {
                return CodeBlock.builder().add("$L.resolve($T.class)", registryVarName, selfWritable).build();
            }
        }

        var builder = CodeBlock.builder().add("$L.resolve($T.class).inject(", registryVarName, resolveType);
        for (var i = 0; i < subtypes.length; i++) {
            if (i > 0) builder.add(", ");
            builder.add(subtypes[i]);
        }

        return builder.add(")").build();
    }

    @Override
    protected CodeBlock whenTypeVar(@NonNull TypeVariable type, TypeMirror _override) {
        return context.isAssignable(type, selfWritable) ?
                CodeBlock.builder()
                        .add("$L.resolve($T.class)", registryVarName, selfWritable)
                        .build() :
                CodeBlock.builder()
                        .add(writerAccessor, typeVarMap.get(type.toString()))
                        .build();
    }

    @Override
    protected CodeBlock whenPrimitiveType(@NonNull PrimitiveType type, TypeMirror override) {
        return getNonGenericCodeBlock(type, override);
    }

    @Override
    protected CodeBlock whenNonGenericType(@NonNull DeclaredType type, TypeMirror override) {
        return getNonGenericCodeBlock(type, override);
    }

    @Override
    protected CodeBlock whenGenericType(@NonNull DeclaredType type, TypeMirror override, @NonNull CodeBlock[] subtypes) {
        TypeMirror resolveType;
        if (override != null) {
            if (context.isNonGeneric(override)) {
                return CodeBlock.builder()
                        .add("$L.resolve($T.class)", registryVarName, override)
                        .build();
            }

            resolveType = override;
        } else {
            resolveType = context.erasure(type);

            if (context.isAssignable(resolveType, selfWritable)) {
                return CodeBlock.builder().add("$L.resolve($T.class)", registryVarName, selfWritable).build();
            }
        }

        var builder = CodeBlock.builder().add("$L.resolve($T.class).inject(", registryVarName, resolveType);
        for (var i = 0; i < subtypes.length; i++) {
            if (i > 0) builder.add(", ");
            builder.add(subtypes[i]);
        }

        return builder.add(")").build();
    }

    @Override
    protected CodeBlock whenArrayPrimitiveType(@NonNull ArrayType type, TypeMirror override) {
        return getNonGenericCodeBlock(type, override);
    }

    @Override
    protected CodeBlock whenArrayType(@NonNull ArrayType type, TypeMirror override, @NonNull CodeBlock subtype) {
        return getGenericCodeBlock(arrayType, override, new CodeBlock[]{subtype});
    }

    @Override
    protected boolean doTraverseArguments(@NonNull DeclaredType type, TypeMirror override) {
        return override != null || !context.isAssignable(context.erasure(type), selfWritable);
    }
}
