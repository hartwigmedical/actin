package com.hartwig.actin.util.json;

import java.time.LocalDate;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

public final class GsonSerializer {

    private GsonSerializer() {
    }

    @NotNull
    public static Gson create() {
        // If we don't register an explicit type adapter for LocalDate, GSON using reflection internally to create serialize these objects
        return new GsonBuilder().serializeNulls()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(LocalDate.class, new GsonLocalDateAdapter())
                .registerTypeHierarchyAdapter(Set.class, new GsonSetAdapter<>())
                .create();
    }
}
