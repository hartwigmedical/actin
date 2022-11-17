package com.hartwig.actin.molecular.orange.evidence.known;


import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.evidence.algo.MutationTypeMatcher;
import com.hartwig.serve.datamodel.range.KnownCodon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CodonLookup {

    private CodonLookup() {
    }

    @Nullable
    public static KnownCodon find(@NotNull Iterable<KnownCodon> knownCodons, @NotNull PurpleVariant variant) {
        for (KnownCodon knownCodon : knownCodons) {
            if (isMatch(knownCodon, variant)) {
                return knownCodon;
            }
        }
        return null;
    }

    private static boolean isMatch(@NotNull KnownCodon codon, @NotNull PurpleVariant variant) {
        boolean geneMatch = codon.gene().equals(variant.gene());
        boolean chromosomeMatch = codon.chromosome().equals(variant.chromosome());
        boolean positionMatch = variant.position() >= codon.start() && variant.position() <= codon.end();
        boolean isTypeMatch = MutationTypeMatcher.matches(variant, codon.applicableMutationType());

        return geneMatch && chromosomeMatch && positionMatch && isTypeMatch;
    }
}
