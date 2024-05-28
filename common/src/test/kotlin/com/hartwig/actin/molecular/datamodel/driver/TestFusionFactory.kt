package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExhaustiveFusion
import com.hartwig.actin.molecular.datamodel.hmf.driver.FusionDriverType

object TestFusionFactory {

    fun createMinimal(): ExhaustiveFusion {
        return ExhaustiveFusion(
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
