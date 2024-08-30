package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType

object TestVirusFactory {

    fun createMinimal(): Virus {
        return Virus(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ClinicalEvidence(),
            name = "",
            type = VirusType.OTHER,
            isReliable = false,
            integrations = 0
        )
    }
}
