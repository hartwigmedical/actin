package com.hartwig.actin.datamodel.molecular.immunology

import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory

object TestHlaAlleleFactory {

    fun createMinimal(): HlaAllele {
        return HlaAllele(
            gene = "",
            alleleGroup = "",
            hlaProtein = "",
            tumorCopyNumber = 1.0,
            hasSomaticMutations = false,
            evidence = TestClinicalEvidenceFactory.createEmpty(),
            event = ""
        )
    }
}