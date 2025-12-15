package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory

object TestHlaAlleleFactory {

    fun createMinimal(): HlaAllele {
        return HlaAllele(
            name = "",
            tumorCopyNumber = 1.0,
            hasSomaticMutations = false,
            isReportable = true,
            driverLikelihood = null,
            evidence = TestClinicalEvidenceFactory.createEmpty(),
            event = ""
        )
    }
}