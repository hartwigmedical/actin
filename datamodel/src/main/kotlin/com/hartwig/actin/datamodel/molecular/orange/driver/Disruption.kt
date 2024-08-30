package com.hartwig.actin.datamodel.molecular.orange.driver

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.sort.driver.DisruptionComparator

data class Disruption(
    val type: DisruptionType,
    val junctionCopyNumber: Double,
    val undisruptedCopyNumber: Double,
    val regionType: RegionType,
    val codingContext: CodingContext,
    val clusterGroup: Int,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?,
) : Driver, GeneAlteration, Comparable<Disruption> {

    override fun compareTo(other: Disruption): Int {
        return DisruptionComparator().compare(this, other)
    }
}
