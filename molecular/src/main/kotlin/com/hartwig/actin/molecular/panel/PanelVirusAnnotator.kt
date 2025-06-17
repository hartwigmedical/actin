package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVirus
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
            driverLikelihood = if (sequencedVirus.virus == "HPV low risk") DriverLikelihood.LOW else DriverLikelihood.HIGH,
            evidence = ExtractionUtil.noEvidence()
        )
    }

    private fun resolveVirusNameAndType(virus: String): Pair<String, VirusType> {
        return when (virus) {
            "EBV" -> Pair("Epstein-Barr virus", VirusType.EPSTEIN_BARR_VIRUS)
            "HBV" -> Pair("Hepatitis B virus", VirusType.HEPATITIS_B_VIRUS)
            "HHV8" -> Pair("Human herpesvirus 8", VirusType.HUMAN_HERPES_VIRUS_8)
            "HPV high risk", "HPV low risk" -> Pair("Human papillomavirus", VirusType.HUMAN_PAPILLOMA_VIRUS)
            "MCV" -> Pair("Merkel cell polyomavirus", VirusType.MERKEL_CELL_VIRUS)
            else -> throw IllegalArgumentException()
        }
    }
}