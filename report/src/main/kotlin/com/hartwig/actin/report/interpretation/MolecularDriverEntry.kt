package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.Displayable
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial

data class MolecularDriverEntry(
    val driverType: String,
    val displayedName: String,
    val eventName: String,
    val driverLikelihood: DriverLikelihood?,
    val actinTrials: Set<String> = emptySet(),
    val externalTrials: Set<ExternalTrial> = emptySet(),
    val bestResponsiveEvidence: String? = null,
    val bestResistanceEvidence: String? = null
) : Displayable {

    override fun display(): String {
        return displayedName
    }
}