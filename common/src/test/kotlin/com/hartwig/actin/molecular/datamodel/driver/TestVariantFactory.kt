package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

object TestVariantFactory {

    fun createMinimal(): Variant {
        return Variant(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ActionableEvidence(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            type = VariantType.SNV,
            variantCopyNumber = 0.0,
            totalCopyNumber = 0.0,
            isBiallelic = false,
            isHotspot = false,
            clonalLikelihood = 0.0,
            canonicalImpact = TestTranscriptImpactFactory.createMinimal(),
            isAssociatedWithDrugResistance = null,
            phaseGroups = null,
            otherImpacts = emptySet(),
            chromosome = "",
            position = 0,
            ref = "",
            alt = "",
        )
    }
}
