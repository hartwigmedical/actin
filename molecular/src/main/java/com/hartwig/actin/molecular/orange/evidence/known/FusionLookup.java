package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.serve.datamodel.fusion.KnownFusion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FusionLookup {

    private FusionLookup() {
    }

    @Nullable
    public static KnownFusion find(@NotNull Iterable<KnownFusion> knownFusions, @NotNull LinxFusion fusion) {
        return null;
    }
}
