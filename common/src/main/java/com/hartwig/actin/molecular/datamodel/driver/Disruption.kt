package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

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
) : Driver, GeneAlteration
