package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVirus
import com.hartwig.actin.datamodel.clinical.SequencedVirusInput
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.molecular.util.ExtractionUtil

class PanelVirusAnnotator {

    fun annotate(viruses: Set<SequencedVirus>): List<Virus> {
        return (viruses.map { createVirus(it) })
    }

    private fun createVirus(sequencedVirus: SequencedVirus): Virus {
        val virusType = resolveVirusNameAndType(sequencedVirus.virus).second
        return Virus(
            name = resolveVirusNameAndType(sequencedVirus.virus).first,
            type = virusType,
            isReliable = true,
            integrations = null,
            event = "$virusType positive",
            isReportable = true,
            driverLikelihood = if (sequencedVirus.virus == SequencedVirusInput.HPV_LOW_RISK) DriverLikelihood.LOW else DriverLikelihood.HIGH,
            evidence = ExtractionUtil.noEvidence()
        )
    }

    private fun resolveVirusNameAndType(sequencedVirusInput: SequencedVirusInput): Pair<String, VirusType> {
        return when (sequencedVirusInput) {
            SequencedVirusInput.EBV -> "Epstein-Barr virus" to VirusType.EPSTEIN_BARR_VIRUS
            SequencedVirusInput.HBV -> "Hepatitis B virus" to VirusType.HEPATITIS_B_VIRUS
            SequencedVirusInput.HHV_8 -> "Human herpesvirus 8" to VirusType.HUMAN_HERPES_VIRUS_8
            SequencedVirusInput.HPV_HIGH_RISK, SequencedVirusInput.HPV_LOW_RISK -> "Human papillomavirus" to VirusType.HUMAN_PAPILLOMA_VIRUS
            SequencedVirusInput.MCV -> "Merkel cell polyomavirus" to VirusType.MERKEL_CELL_VIRUS
        }
    }
}