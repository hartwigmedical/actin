package com.hartwig.actin.molecular.evidence.orange

import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria

object MolecularRecordAnnotatorFunctions {

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
        isReportable = variant.isReportable
    )
}