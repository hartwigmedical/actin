package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

object TestVirusFactory {

    fun createMinimal(): Virus {
        return Virus(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ActionableEvidence(),
            name = "",
            type = VirusType.OTHER,
            isReliable = false,
            integrations = 0
        )
    }
}
