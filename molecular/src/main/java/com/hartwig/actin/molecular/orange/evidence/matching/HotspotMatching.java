package com.hartwig.actin.molecular.orange.evidence.matching;

import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.serve.datamodel.hotspot.VariantHotspot;

import org.jetbrains.annotations.NotNull;

public final class HotspotMatching {

    private HotspotMatching() {
    }

    public static boolean isMatch(@NotNull VariantHotspot hotspot, @NotNull PurpleVariant variant) {
        boolean geneMatch = hotspot.gene().equals(variant.gene());
        boolean chromosomeMatch = hotspot.chromosome().equals(variant.chromosome());
        boolean positionMatch = hotspot.position() == variant.position();
        boolean refMatch = hotspot.ref().equals(variant.ref());
        boolean altMatch = hotspot.alt().equals(variant.alt());

        return geneMatch && chromosomeMatch && positionMatch && refMatch && altMatch;
    }
}
