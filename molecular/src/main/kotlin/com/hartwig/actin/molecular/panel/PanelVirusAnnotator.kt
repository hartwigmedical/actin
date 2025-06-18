package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVirus
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.molecular.util.ExtractionUtil

class PanelVirusAnnotator {

    fun annotate(viruses: Set<SequencedVirus>): List<Virus> {
        return viruses.map { createVirus(it) }
    }

    private fun createVirus(sequencedVirus: SequencedVirus): Virus {
        return Virus(
            name = sequencedVirus.type.display(),
            type = sequencedVirus.type,
            isReliable = true,
            integrations = null,
            event = "${sequencedVirus.type} positive",
            isReportable = true,
            driverLikelihood = if (sequencedVirus.isLowRisk) DriverLikelihood.LOW else DriverLikelihood.HIGH,
            evidence = ExtractionUtil.noEvidence()
        )
    }
}