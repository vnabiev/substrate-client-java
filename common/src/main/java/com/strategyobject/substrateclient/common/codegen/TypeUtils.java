package com.strategyobject.substrateclient.common.codegen;

import lombok.val;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.strategyobject.substrateclient.common.utils.StringUtils.capitalize;

public class TypeUtils {
    private static final String SETTER_PREFIX = "set";
    private static final String DEFAULT_GETTER_PREFIX = "get";
    private static final String BOOLEAN_GETTER_PREFIX = "is";

    public static String getSetterName(String field) {
        return SETTER_PREFIX + capitalize(field);
    }

    public static String getGetterName(VariableElement field) {
        val isPrimitiveBoolean = field.asType().getKind() == TypeKind.BOOLEAN;
        val fieldName = field.getSimpleName().toString();
        if (isPrimitiveBoolean && fieldName.startsWith(BOOLEAN_GETTER_PREFIX)) {
            return fieldName;
        }

        val prefix = isPrimitiveBoolean ?
                BOOLEAN_GETTER_PREFIX :
                DEFAULT_GETTER_PREFIX;

        return prefix + capitalize(fieldName);
    }

    public static String getSimpleName(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return type.toString();
        }

        if (type instanceof DeclaredType) {
            return ((DeclaredType) type).asElement().getSimpleName().toString();
        }

        if (type instanceof ArrayType) {
            return String.format("%s[]", getSimpleName(((ArrayType) type).getComponentType()));
        }

        throw new IllegalArgumentException(String.format("Cannot populate the name of %s", type));
    }
}
