package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator

data class CopyNumber(
    val type: CopyNumberType,
    val minCopies: Int,
    val maxCopies: Int,
    override val isReportable: Boolean,
    override val isAssociatedWithDrugResistance: Boolean?,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect
) : Driver, GeneAlteration, Comparable<CopyNumber> {

    override fun compareTo(other: CopyNumber): Int {
        return CopyNumberComparator().compare(this, other)
    }
}
