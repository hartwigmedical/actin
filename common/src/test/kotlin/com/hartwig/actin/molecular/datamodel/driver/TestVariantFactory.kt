package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

object TestVariantFactory {

    fun createMinimal(): Variant {
        return Variant(
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
            isHotspot = false,
            canonicalImpact = TestTranscriptImpactFactory.createMinimal(),
            isAssociatedWithDrugResistance = null,
        )
    }
}
