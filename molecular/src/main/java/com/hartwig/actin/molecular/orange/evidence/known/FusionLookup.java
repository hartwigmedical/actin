package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.evidence.matching.FusionMatching;
import com.hartwig.hmftools.datamodel.linx.LinxFusion;
import com.hartwig.serve.datamodel.fusion.KnownFusion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class FusionLookup {

    private FusionLookup() {
    }

    @Nullable
    public static KnownFusion find(@NotNull Iterable<KnownFusion> knownFusions, @NotNull LinxFusion fusion) {
        KnownFusion best = null;
        for (KnownFusion knownFusion : knownFusions) {
            if (FusionMatching.isGeneMatch(knownFusion, fusion)) {
                boolean matchesExonUp = FusionMatching.explicitlyMatchesExonUp(knownFusion, fusion);
                boolean matchesExonDown = FusionMatching.explicitlyMatchesExonDown(knownFusion, fusion);
                if (matchesExonUp && matchesExonDown) {
                    best = knownFusion;
                } else {
                    boolean meetsExonUp = matchesExonUp && !hasDownRequirements(knownFusion);
                    boolean meetsExonDown = matchesExonDown && !hasUpRequirements(knownFusion);
                    if (meetsExonUp || meetsExonDown) {
                        if (best == null || !hasUpRequirements(best) || !hasDownRequirements(best)) {
                            best = knownFusion;
                        }
                    } else if (!hasUpRequirements(knownFusion) && !hasDownRequirements(knownFusion)) {
                        best = knownFusion;
                    }
                }
            }
        }

        return best;
    }

    private static boolean hasUpRequirements(@NotNull KnownFusion knownFusion) {
        return knownFusion.minExonUp() != null && knownFusion.maxExonUp() != null;
    }

    private static boolean hasDownRequirements(@NotNull KnownFusion knownFusion) {
        return knownFusion.minExonDown() != null && knownFusion.maxExonDown() != null;
    }
}
