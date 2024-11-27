package com.hartwig.actin.datamodel.molecular.orange.driver

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.sort.driver.CopyNumberComparator

data class CopyNumber(
    // TODO (KD): Change datamodel to 1) canonicalImpact (type, minCopies, maxCopies) 2) otherImpacts (type, minCopies, maxCopies)
    val type: CopyNumberType,
    val minCopies: Int,
    val maxCopies: Int,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?
) : Driver, GeneAlteration, Comparable<CopyNumber> {

    override fun compareTo(other: CopyNumber): Int {
        return CopyNumberComparator().compare(this, other)
    }
}
