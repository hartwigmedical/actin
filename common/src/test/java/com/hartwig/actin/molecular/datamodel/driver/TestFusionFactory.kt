package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

object TestFusionFactory {

    fun createMinimal(): Fusion {
        return Fusion(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ActionableEvidence(),
            isAssociatedWithDrugResistance = null,
            geneStart = "",
            geneTranscriptStart = "",
            fusedExonUp = -1,
            geneEnd = "",
            geneTranscriptEnd = "",
            fusedExonDown = -1,
            proteinEffect = ProteinEffect.UNKNOWN,
            driverType = FusionDriverType.KNOWN_PAIR
        )
    }
}
