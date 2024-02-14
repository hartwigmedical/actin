package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.serve.datamodel.MutationType
import com.hartwig.serve.datamodel.gene.GeneAnnotation

object GeneMatching {

    fun isMatch(geneAnnotation: GeneAnnotation, variant: Variant): Boolean {
        val geneMatch = geneAnnotation.gene() == variant.gene
        val typeMatch = MutationTypeMatching.matches(MutationType.ANY, variant)
        return geneMatch && typeMatch
    }
}
