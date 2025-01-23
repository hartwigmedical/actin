package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingContext
import org.apache.logging.log4j.util.Strings

object TestDisruptionFactory {

    fun createMinimal(): Disruption {
        return Disruption(
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = TestClinicalEvidenceFactory.createEmpty(),
            gene = Strings.EMPTY,
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null,
            type = DisruptionType.BND,
            junctionCopyNumber = 0.0,
            undisruptedCopyNumber = 0.0,
            regionType = RegionType.INTRONIC,
            codingContext = CodingContext.NON_CODING,
            clusterGroup = 0
        )
    }
}
