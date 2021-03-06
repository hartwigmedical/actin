package com.hartwig.actin.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

public final class GsonSerializer {

    private GsonSerializer() {
    }

    @NotNull
    public static Gson create() {
        return new GsonBuilder().serializeNulls().enableComplexMapKeySerialization().create();
    }
}
