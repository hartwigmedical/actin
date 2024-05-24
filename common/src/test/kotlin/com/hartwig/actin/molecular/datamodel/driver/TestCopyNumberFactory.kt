package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.wgs.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.wgs.driver.CopyNumberType

object TestCopyNumberFactory {

    fun createMinimal(): CopyNumber {
        return CopyNumber(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ActionableEvidence(),
            isAssociatedWithDrugResistance = null,
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            type = CopyNumberType.NONE,
            minCopies = 0,
            maxCopies = 0
        )
    }
}
