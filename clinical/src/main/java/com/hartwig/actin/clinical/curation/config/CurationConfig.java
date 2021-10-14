package com.hartwig.actin.clinical.curation.config;

import org.jetbrains.annotations.NotNull;

public interface CurationConfig {

    @NotNull
    String input();

    boolean ignore();
}
