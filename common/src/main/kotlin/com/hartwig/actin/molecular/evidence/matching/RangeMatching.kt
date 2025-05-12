package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.serve.datamodel.molecular.range.RangeAnnotation

object RangeMatching {

    fun isMatch(rangeAnnotation: RangeAnnotation, variant: Variant): Boolean {
        val geneMatch = rangeAnnotation.gene() == variant.gene
        val chromosomeMatch = rangeAnnotation.chromosome() == variant.chromosome
        val positionMatch = variant.position.let { it >= rangeAnnotation.start() && it <= rangeAnnotation.end() }
        val typeMatch = MutationTypeMatching.matches(rangeAnnotation.applicableMutationType(), variant)

        return geneMatch && chromosomeMatch && positionMatch && typeMatch
    }
}
