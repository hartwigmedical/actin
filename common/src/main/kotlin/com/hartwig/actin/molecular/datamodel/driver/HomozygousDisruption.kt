package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.sort.driver.HomozygousDisruptionComparator

data class HomozygousDisruption(
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?
) : Driver, GeneAlteration, Comparable<HomozygousDisruption> {

    override fun compareTo(other: HomozygousDisruption): Int {
        return HomozygousDisruptionComparator().compare(this, other)
    }
}
