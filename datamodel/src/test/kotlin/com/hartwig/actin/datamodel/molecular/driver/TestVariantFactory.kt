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
}
