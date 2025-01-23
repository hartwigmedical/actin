package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.Displayable
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceTier
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial

data class MolecularDriverEntry(
    val driverType: String,
    val description: String,
    val event: String,
    val driverLikelihood: DriverLikelihood?,
    val evidenceTier: EvidenceTier,
    val proteinEffect: ProteinEffect? = null,
    val actinTrials: Set<String> = emptySet(),
    val externalTrials: Set<ExternalTrial> = emptySet(),
    val bestResponsiveEvidence: String? = null,
    val bestResistanceEvidence: String? = null
) : Displayable {

    override fun display(): String {
        return description
    }
}