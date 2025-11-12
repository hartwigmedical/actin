package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class MicrosatelliteStability(
    val microsatelliteIndelsPerMb: Double?,
    val isUnstable: Boolean,
    override val evidence: ClinicalEvidence,
    override val event: String = if (isUnstable) MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE else MolecularCharacteristicEvents.MICROSATELLITE_STABLE
) : Actionable