package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood

data class MolecularDriverEntry(
    val driverType: String,
    val driver: String,
    val driverLikelihood: DriverLikelihood?,
    val actinTrials: Set<String>,
    val externalTrials: Set<String>,
    val bestResponsiveEvidence: String?,
    val bestResistanceEvidence: String?
)