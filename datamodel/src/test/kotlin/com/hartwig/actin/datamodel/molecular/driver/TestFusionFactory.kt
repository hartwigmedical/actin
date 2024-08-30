package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType

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
