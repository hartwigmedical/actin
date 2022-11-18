package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.evidence.algo.MutationTypeMatcher;
import com.hartwig.serve.datamodel.range.KnownExon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ExonLookup {

    private ExonLookup() {
    }

    @Nullable
    public static KnownExon find(@NotNull Iterable<KnownExon> knownExons, @NotNull PurpleVariant variant) {
        for (KnownExon knownExon : knownExons) {
            if (isMatch(knownExon, variant)) {
                return knownExon;
            }
        }
        return null;
    }

    private static boolean isMatch(@NotNull KnownExon exon, @NotNull PurpleVariant variant) {
        boolean geneMatch = exon.gene().equals(variant.gene());
        boolean chromosomeMatch = exon.chromosome().equals(variant.chromosome());
        boolean positionMatch = variant.position() >= exon.start() && variant.position() <= exon.end();
        boolean isTypeMatch = MutationTypeMatcher.matches(variant, exon.applicableMutationType());

        return geneMatch && chromosomeMatch && positionMatch && isTypeMatch;
    }
}
