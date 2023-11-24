package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.range.RangeAnnotation

object RangeMatching {

    fun isMatch(rangeAnnotation: RangeAnnotation, variant: PurpleVariant): Boolean {
        val geneMatch = rangeAnnotation.gene() == variant.gene()
        val chromosomeMatch = rangeAnnotation.chromosome() == variant.chromosome()
        val positionMatch = variant.position() >= rangeAnnotation.start() && variant.position() <= rangeAnnotation.end()
        val typeMatch = MutationTypeMatching.matches(rangeAnnotation.applicableMutationType(), variant)

        return geneMatch && chromosomeMatch && positionMatch && typeMatch
    }
}
