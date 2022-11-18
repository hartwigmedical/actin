package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.evidence.matching.RangeMatching;
import com.hartwig.serve.datamodel.range.KnownExon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ExonLookup {

    private ExonLookup() {
    }

    @Nullable
    public static KnownExon find(@NotNull Iterable<KnownExon> knownExons, @NotNull PurpleVariant variant) {
        for (KnownExon knownExon : knownExons) {
            if (RangeMatching.isMatch(knownExon, variant)) {
                return knownExon;
            }
        }
        return null;
    }
}
