package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.serve.datamodel.range.KnownCodon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CodonLookup {

    private CodonLookup() {
    }

    @Nullable
    public static KnownCodon find(@NotNull Iterable<KnownCodon> knownCodons, @NotNull PurpleVariant variant) {
        return null;
    }
}
