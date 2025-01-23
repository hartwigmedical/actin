package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory

object TestFusionFactory {

    fun createMinimal(): Fusion {
        return Fusion(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = TestClinicalEvidenceFactory.createEmpty(),
            geneStart = "",
            geneEnd = "",
            driverType = FusionDriverType.KNOWN_PAIR,
            proteinEffect = ProteinEffect.NO_EFFECT,
            isAssociatedWithDrugResistance = null,
            geneTranscriptStart = null,
            geneTranscriptEnd = null,
            fusedExonUp = null,
            fusedExonDown = null
        )
    }
}
