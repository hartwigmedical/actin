package com.hartwig.actin.database;

import org.jetbrains.annotations.NotNull;

public interface DatabaseLoaderConfig {

    @NotNull
    String dbUser();

    @NotNull
    String dbPass();

    @NotNull
    String dbUrl();
}
