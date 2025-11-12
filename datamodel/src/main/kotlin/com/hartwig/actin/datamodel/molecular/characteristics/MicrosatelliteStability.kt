package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents.MICROSATELLITE_STABLE
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class MicrosatelliteStability(
    val microsatelliteIndelsPerMb: Double?,
    val isUnstable: Boolean,
    override val evidence: ClinicalEvidence
) : Actionable {
    override fun eventName(): String {
        return if (isUnstable) MICROSATELLITE_UNSTABLE else MICROSATELLITE_STABLE
    }
}
