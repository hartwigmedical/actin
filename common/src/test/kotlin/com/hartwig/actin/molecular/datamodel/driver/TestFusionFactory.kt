package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType

object TestFusionFactory {

    fun createMinimal(): Fusion {
        return Fusion(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ClinicalEvidence(),
            geneStart = "",
            geneEnd = "",
            driverType = FusionDriverType.KNOWN_PAIR,
            proteinEffect = ProteinEffect.NO_EFFECT,
            isAssociatedWithDrugResistance = null
        )
    }

    fun createMinimalExtended(): ExtendedFusionDetails {
        return ExtendedFusionDetails(
            geneTranscriptStart = "",
            geneTranscriptEnd = "",
            fusedExonDown = 0,
            fusedExonUp = 0,
        )
    }
}
