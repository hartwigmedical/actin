package com.hartwig.actin.clinical.curation;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class CurationUtil {

    private static final String DOID_SEPARATOR = ";";

    private CurationUtil() {
    }

    @NotNull
    public static Set<String> toDOIDs(@NotNull String doidString) {
        return Sets.newHashSet(doidString.split(DOID_SEPARATOR));
    }
}
