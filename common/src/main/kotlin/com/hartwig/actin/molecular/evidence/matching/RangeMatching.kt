package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.serve.datamodel.range.RangeAnnotation

object RangeMatching {

    fun isMatch(rangeAnnotation: RangeAnnotation, variant: VariantMatchCriteria): Boolean {
        val geneMatch = rangeAnnotation.gene() == variant.gene
        val chromosomeMatch = rangeAnnotation.chromosome() == variant.chromosome
        val positionMatch = variant.position?.let { it >= rangeAnnotation.start() && it <= rangeAnnotation.end() } == true
        val typeMatch = MutationTypeMatching.matches(rangeAnnotation.applicableMutationType(), variant)

        return geneMatch && chromosomeMatch && positionMatch && typeMatch
    }
}
