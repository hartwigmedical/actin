package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.wgs.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.wgs.driver.Disruption
import com.hartwig.actin.molecular.datamodel.wgs.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.wgs.driver.RegionType
import org.apache.logging.log4j.util.Strings

object TestDisruptionFactory {

    fun createMinimal(): Disruption {
        return Disruption(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ActionableEvidence(),
            isAssociatedWithDrugResistance = null,
            gene = Strings.EMPTY,
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            type = DisruptionType.BND,
            junctionCopyNumber = 0.0,
            undisruptedCopyNumber = 0.0,
            regionType = RegionType.INTRONIC,
            codingContext = CodingContext.NON_CODING,
            clusterGroup = 0
        )
    }
}
