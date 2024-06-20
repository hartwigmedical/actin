package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedVariantDetails

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
            isAssociatedWithDrugResistance = null,
            chromosome = "",
            position = 0,
            ref = "",
            alt = "",
            type = VariantType.SNV,
            isHotspot = false,
            canonicalImpact = TestTranscriptImpactFactory.createMinimal(),
        )
    }

    fun createMinimalExtended(): ExtendedVariantDetails {
        return ExtendedVariantDetails(
            clonalLikelihood = 0.0,
            variantCopyNumber = 0.0,
            totalCopyNumber = 0.0,
            isBiallelic = false,
            otherImpacts = emptySet(),
            phaseGroups = null,
        )
    }
}
