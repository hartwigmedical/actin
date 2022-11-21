package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.evidence.matching.FusionMatching;
import com.hartwig.serve.datamodel.fusion.KnownFusion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class FusionLookup {

    private FusionLookup() {
    }

    @Nullable
    public static KnownFusion find(@NotNull Iterable<KnownFusion> knownFusions, @NotNull LinxFusion fusion) {
        KnownFusion genesMatch = null;
        for (KnownFusion knownFusion : knownFusions) {
            if (FusionMatching.isGeneMatch(knownFusion, fusion)) {
                genesMatch = knownFusion;
                if (FusionMatching.isExonMatch(knownFusion, fusion)) {
                    return knownFusion;
                }
            }
        }
        return genesMatch;
    }
}
