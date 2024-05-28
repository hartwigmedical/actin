package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExhaustiveVariant

object TestVariantFactory {

    fun createMinimal(): ExhaustiveVariant {
        return ExhaustiveVariant(
            chromosome = "",
            position = 0,
            ref = "",
            alt = "",
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
        )
    }
}
