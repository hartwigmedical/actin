package com.hartwig.actin.molecular.orange.evidence.matching;

import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.serve.datamodel.range.RangeAnnotation;

import org.jetbrains.annotations.NotNull;

public final class RangeMatching {

    private RangeMatching() {
    }

    public static boolean isMatch(@NotNull RangeAnnotation rangeAnnotation, @NotNull PurpleVariant variant) {
        boolean geneMatch = rangeAnnotation.gene().equals(variant.gene());
        boolean chromosomeMatch = rangeAnnotation.chromosome().equals(variant.chromosome());
        boolean positionMatch = variant.position() >= rangeAnnotation.start() && variant.position() <= rangeAnnotation.end();
        boolean typeMatch = MutationTypeMatching.matches(rangeAnnotation.applicableMutationType(), variant);

        return geneMatch && chromosomeMatch && positionMatch && typeMatch;
    }
}
