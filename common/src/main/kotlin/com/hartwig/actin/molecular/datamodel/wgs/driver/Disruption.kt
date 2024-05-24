package com.hartwig.actin.molecular.datamodel.wgs.driver

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.sort.driver.DisruptionComparator

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
    override val evidence: ActionableEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?,
) : Driver, GeneAlteration, Comparable<Disruption> {

    override fun compareTo(other: Disruption): Int {
        return DisruptionComparator().compare(this, other)
    }
}
