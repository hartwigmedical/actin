package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.orange.driver.Disruption
import com.hartwig.actin.molecular.datamodel.orange.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.orange.driver.RegionType
import com.hartwig.actin.molecular.datamodel.orange.driver.Virus
import com.hartwig.actin.molecular.datamodel.orange.driver.VirusType

object TestMolecularFactory {

    fun minimalDisruption(): Disruption {
        return Disruption(
            type = DisruptionType.INS,
            junctionCopyNumber = 0.0,
            undisruptedCopyNumber = 0.0,
            regionType = RegionType.INTRONIC,
            codingContext = CodingContext.NON_CODING,
            clusterGroup = 0,
            isReportable = false,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ClinicalEvidence(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = false
        )
    }

    fun minimalCopyNumber(): CopyNumber {
        return CopyNumber(
            type = CopyNumberType.NONE,
            minCopies = 0,
            maxCopies = 0,
            isReportable = false,
            isAssociatedWithDrugResistance = false,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ClinicalEvidence(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN
        )
    }

    fun minimalHomozygousDisruption(): HomozygousDisruption {
        return HomozygousDisruption(
            isReportable = false,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ClinicalEvidence(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = false
        )
    }

    fun minimalVirus(): Virus {
        return Virus(
            name = "",
            type = VirusType.OTHER,
            isReliable = false,
            integrations = 0,
            isReportable = false,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ClinicalEvidence(),
        )
    }
}