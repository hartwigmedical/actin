package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.serve.datamodel.hotspot.KnownHotspot;
import com.hartwig.serve.datamodel.hotspot.VariantHotspot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HotspotLookup {

    private HotspotLookup() {
    }

    @Nullable
    public static KnownHotspot find(@NotNull Iterable<KnownHotspot> knownHotspots, @NotNull PurpleVariant variant) {
        for (KnownHotspot knownHotspot : knownHotspots) {
            if (isMatch(knownHotspot, variant)) {
                return knownHotspot;
            }
        }

        return null;
    }

    private static boolean isMatch(@NotNull VariantHotspot hotspot, @NotNull PurpleVariant variant) {
        boolean geneMatch = hotspot.gene().equals(variant.gene());
        boolean chromosomeMatch = hotspot.chromosome().equals(variant.chromosome());
        boolean positionMatch = hotspot.position() == variant.position();
        boolean refMatch = hotspot.ref().equals(variant.ref());
        boolean altMatch = hotspot.alt().equals(variant.alt());

        return geneMatch && chromosomeMatch && positionMatch && refMatch && altMatch;
    }
}
