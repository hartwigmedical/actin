package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
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
            if (knownFusion.geneUp().equals(fusion.geneStart()) && knownFusion.geneDown().equals(fusion.geneEnd())) {
                genesMatch = knownFusion;
                if (isExonMatch(knownFusion, fusion)) {
                    return knownFusion;
                }
            }
        }
        return genesMatch;
    }

    private static boolean isExonMatch(@NotNull KnownFusion knownFusion, @NotNull LinxFusion fusion) {
        Integer minExonUp = knownFusion.minExonUp();
        Integer maxExonUp = knownFusion.maxExonUp();

        Integer minExonDown = knownFusion.minExonDown();
        Integer maxExonDown = knownFusion.maxExonDown();

        if (minExonUp != null && maxExonUp != null && minExonDown != null && maxExonDown != null) {
            return fusion.fusedExonUp() >= minExonUp && fusion.fusedExonUp() <= minExonUp && fusion.fusedExonDown() >= minExonDown
                    && fusion.fusedExonDown() <= maxExonDown;
        }

        return false;
    }
}
