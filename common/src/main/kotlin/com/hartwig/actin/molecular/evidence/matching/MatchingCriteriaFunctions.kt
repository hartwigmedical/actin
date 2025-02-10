package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant

object MatchingCriteriaFunctions {

    fun createFusionCriteria(fusion: Fusion) = FusionMatchCriteria(
        isReportable = fusion.isReportable,
        geneStart = fusion.geneStart,
        geneEnd = fusion.geneEnd,
        driverType = fusion.driverType
    )

    fun createVariantCriteria(variant: Variant) = VariantMatchCriteria(
        gene = variant.gene,
        chromosome = variant.chromosome,
        position = variant.position,
        ref = variant.ref,
        alt = variant.alt,
        type = variant.type,
        codingEffect = variant.canonicalImpact.codingEffect,
        driverLikelihood = variant.driverLikelihood,
        isReportable = variant.isReportable
    )
}