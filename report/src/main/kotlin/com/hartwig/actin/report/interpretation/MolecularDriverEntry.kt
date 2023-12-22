package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial

data class MolecularDriverEntry(
    val driverType: String,
    val driver: String,
    val driverLikelihood: DriverLikelihood?,
    val actinTrials: Set<String> = emptySet(),
    val externalTrials: Set<ExternalTrial> = emptySet(),
    val bestResponsiveEvidence: String? = null,
    val bestResistanceEvidence: String? = null
)