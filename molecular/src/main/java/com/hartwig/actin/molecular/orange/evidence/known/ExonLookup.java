package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.serve.datamodel.range.KnownExon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ExonLookup {

    private ExonLookup() {
    }

    @Nullable
    public static KnownExon find(@NotNull Iterable<KnownExon> knownExons, @NotNull PurpleVariant variant) {
        return null;
    }
}
