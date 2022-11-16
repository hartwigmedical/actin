package com.hartwig.actin.molecular.filter;

import org.jetbrains.annotations.NotNull;

public interface GeneFilter {

    boolean include(@NotNull String gene);

    int size();
}
