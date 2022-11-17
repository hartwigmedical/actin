package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.serve.datamodel.hotspot.KnownHotspot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HotspotLookup {

    private HotspotLookup() {
    }

    @Nullable
    public static KnownHotspot find(@NotNull Iterable<KnownHotspot> knownHotspots, @NotNull PurpleVariant variant) {
        return null;
    }
}
