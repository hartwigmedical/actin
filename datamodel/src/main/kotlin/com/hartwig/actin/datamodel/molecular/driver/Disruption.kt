package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

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
        return Comparator.comparing<Disruption, Disruption>({ it }, DriverComparator())
            .thenComparing({ it }, GeneAlterationComparator())
            .thenComparing({ it.type.toString() }, String::compareTo)
            .thenComparing(Disruption::junctionCopyNumber, reverseOrder())
            .thenComparing(Disruption::undisruptedCopyNumber)
            .compare(this, other)
    }
}
