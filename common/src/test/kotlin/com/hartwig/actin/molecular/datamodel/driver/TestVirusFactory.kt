package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.hmf.driver.Virus
import com.hartwig.actin.molecular.datamodel.hmf.driver.VirusType

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
