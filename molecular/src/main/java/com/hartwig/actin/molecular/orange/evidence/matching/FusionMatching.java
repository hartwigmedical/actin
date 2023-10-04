package com.hartwig.actin.molecular.orange.evidence.matching;

import com.hartwig.hmftools.datamodel.linx.LinxFusion;
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

        boolean meetsExonUp = minExonUp == null || maxExonUp == null || explicitlyMatchesExonUp(fusionPair, fusion);

        Integer minExonDown = fusionPair.minExonDown();
        Integer maxExonDown = fusionPair.maxExonDown();

        boolean meetsExonDown = minExonDown == null || maxExonDown == null || explicitlyMatchesExonDown(fusionPair, fusion);

        return meetsExonUp && meetsExonDown;
    }

    public static boolean explicitlyMatchesExonUp(@NotNull FusionPair fusionPair, @NotNull LinxFusion fusion) {
        Integer minExonUp = fusionPair.minExonUp();
        Integer maxExonUp = fusionPair.maxExonUp();

        if (minExonUp == null || maxExonUp == null) {
            return false;
        }

        return fusion.fusedExonUp() >= minExonUp && fusion.fusedExonUp() <= maxExonUp;
    }

    public static boolean explicitlyMatchesExonDown(@NotNull FusionPair fusionPair, @NotNull LinxFusion fusion) {
        Integer minExonDown = fusionPair.minExonDown();
        Integer maxExonDown = fusionPair.maxExonDown();

        if (minExonDown == null || maxExonDown == null) {
            return false;
        }

        return fusion.fusedExonDown() >= minExonDown && fusion.fusedExonDown() <= maxExonDown;
    }
}
