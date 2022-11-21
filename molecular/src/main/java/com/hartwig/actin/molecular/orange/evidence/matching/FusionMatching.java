package com.hartwig.actin.molecular.orange.evidence.matching;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.serve.datamodel.fusion.FusionPair;

import org.jetbrains.annotations.NotNull;

public final class FusionMatching {

    private FusionMatching() {
    }

    public static boolean isGeneMatch(@NotNull FusionPair fusionPair, @NotNull LinxFusion fusion) {
        return fusionPair.geneUp().equals(fusion.geneStart()) && fusionPair.geneDown().equals(fusion.geneEnd());
    }

    public static boolean isExonMatch(@NotNull FusionPair fusionPair, @NotNull LinxFusion fusion) {
        Integer minExonUp = fusionPair.minExonUp();
        Integer maxExonUp = fusionPair.maxExonUp();

        Integer minExonDown = fusionPair.minExonDown();
        Integer maxExonDown = fusionPair.maxExonDown();

        if (minExonUp != null && maxExonUp != null && minExonDown != null && maxExonDown != null) {
            return fusion.fusedExonUp() >= minExonUp && fusion.fusedExonUp() <= minExonUp && fusion.fusedExonDown() >= minExonDown
                    && fusion.fusedExonDown() <= maxExonDown;
        }

        return false;
    }
}
