package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption

object TestHomozygousDisruptionFactory {

    fun createMinimal(): HomozygousDisruption {
        return HomozygousDisruption(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ActionableEvidence(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null
        )
    }
}
