package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory

object TestVariantFactory {

    fun createMinimal(): Variant {
        return Variant(
            chromosome = "",
            position = 0,
            ref = "",
            alt = "",
            type = VariantType.SNV,
            canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal(),
            otherImpacts = emptySet(),
            isCancerAssociatedVariant = false,
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = TestClinicalEvidenceFactory.createEmpty(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null,
        )
    }

    fun createMinimalExtended(): ExtendedVariantDetails {
        return ExtendedVariantDetails(
            clonalLikelihood = 1.0,
            variantCopyNumber = 0.0,
            totalCopyNumber = 0.0,
            isBiallelic = false,
            phaseGroups = null,
        )
    }
}
