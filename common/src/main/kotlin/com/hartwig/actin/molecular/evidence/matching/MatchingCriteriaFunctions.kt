package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant

object MatchingCriteriaFunctions {

    fun createFusionCriteria(fusion: Fusion) = FusionMatchCriteria(
        isReportable = fusion.isReportable,
        geneStart = fusion.geneStart,
        geneEnd = fusion.geneEnd,
        fusedExonUp = fusion.fusedExonUp,
        fusedExonDown = fusion.fusedExonDown,
        driverType = fusion.driverType
    )

    fun createVariantCriteria(variant: Variant) = VariantMatchCriteria(
        gene = variant.gene,
        codingEffect = variant.canonicalImpact.codingEffect,
        type = variant.type,
        chromosome = variant.chromosome,
        position = variant.position,
        ref = variant.ref,
        alt = variant.alt,
        driverLikelihood = variant.driverLikelihood,
        isReportable = variant.isReportable
    )
}