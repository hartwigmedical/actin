package com.hartwig.actin.molecular.panel

import com.hartwig.actin.configuration.MolecularConfiguration
import com.hartwig.actin.datamodel.clinical.SequencedVirus
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.molecular.util.ExtractionUtil

class PanelVirusAnnotator(private val molecularConfiguration: MolecularConfiguration) {

    fun annotate(viruses: Set<SequencedVirus>): List<Virus> {
        return viruses.map { createVirus(it, molecularConfiguration.eventPathogenicityIsConfirmed) }
    }

    private fun createVirus(sequencedVirus: SequencedVirus, eventPathogenicityIsConfirmed: Boolean = false): Virus {
        return Virus(
            name = sequencedVirus.type.display(),
            type = sequencedVirus.type,
            isReliable = true,
            integrations = null,
            event = "${sequencedVirus.type} positive",
            isReportable = true,
            driverLikelihood = if (eventPathogenicityIsConfirmed || !sequencedVirus.isLowRisk) {
                DriverLikelihood.HIGH
            } else {
                DriverLikelihood.LOW
            },
            evidence = ExtractionUtil.noEvidence()
        )
    }
}