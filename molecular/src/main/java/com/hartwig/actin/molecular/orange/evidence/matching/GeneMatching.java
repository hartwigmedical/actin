package com.hartwig.actin.molecular.orange.evidence.matching;

import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.serve.datamodel.MutationType;
import com.hartwig.serve.datamodel.gene.GeneAnnotation;

import org.jetbrains.annotations.NotNull;

public final class GeneMatching {

    private GeneMatching() {
    }

    public static boolean isMatch(@NotNull GeneAnnotation geneAnnotation, @NotNull PurpleVariant variant) {
        boolean geneMatch = geneAnnotation.gene().equals(variant.gene());
        boolean typeMatch = MutationTypeMatching.matches(MutationType.ANY, variant);

        return geneMatch && typeMatch;
    }
}
