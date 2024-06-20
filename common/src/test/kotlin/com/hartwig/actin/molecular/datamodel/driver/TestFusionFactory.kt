package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType

object TestFusionFactory {

    fun createMinimal(): Fusion {
        return Fusion(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ActionableEvidence(),
            geneStart = "",
            geneEnd = "",
            geneTranscriptStart = "",
            geneTranscriptEnd = "",
            driverType = FusionDriverType.KNOWN_PAIR,
            proteinEffect = ProteinEffect.NO_EFFECT
        )
    }

    fun createMinimalExtended(): ExtendedFusionDetails {
        return ExtendedFusionDetails(
            fusedExonDown = 0,
            fusedExonUp = 0,
            isAssociatedWithDrugResistance = null
        )
    }
}
