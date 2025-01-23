package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory

object TestVirusFactory {

    fun createMinimal(): Virus {
        return Virus(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = TestClinicalEvidenceFactory.createEmpty(),
            name = "",
            type = VirusType.OTHER,
            isReliable = false,
            integrations = 0
        )
    }
}
