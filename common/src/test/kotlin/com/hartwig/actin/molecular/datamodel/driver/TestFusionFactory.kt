package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

object TestFusionFactory {

    fun createMinimal(): Fusion {
        return Fusion(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ActionableEvidence(),
            geneStart = "",
            geneTranscriptStart = "",
            geneEnd = "",
            geneTranscriptEnd = "",
        )
    }
}
