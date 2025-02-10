package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.report.interpretation.MolecularCharacteristicFormat

object MolecularClinicalEvidenceFunctions {

    fun molecularEvidenceByEvent(molecularHistory: MolecularHistory): Set<Pair<String, ClinicalEvidence>> {
        val allDrivers =
            DriverTableFunctions.allDrivers(molecularHistory).flatMap { it.second }.toSortedSet(Comparator.comparing { it.event })
        val allMSI =
            extractCharacteristics(
                molecularHistory,
                { "MS ${MolecularCharacteristicFormat.formatMicrosatelliteStability(it)}" },
                { it.microsatelliteEvidence })
        val allTML =
            extractCharacteristics(
                molecularHistory,
                { MolecularCharacteristicFormat.formatTumorMutationalLoad(it, false) },
                { it.tumorMutationalLoadEvidence })
        val allTMB =
            extractCharacteristics(
                molecularHistory,
                { MolecularCharacteristicFormat.formatTumorMutationalBurden(it, false) },
                { it.tumorMutationalBurdenEvidence })
        val allHRD =
            extractCharacteristics(
                molecularHistory,
                { "HR ${MolecularCharacteristicFormat.formatHomologousRecombination(it, false)}" },
                { it.homologousRecombinationEvidence })
        return allMSI + allTMB + allTML + allHRD + allDrivers.map { it.event to it.evidence }
    }

    private fun extractCharacteristics(
        molecularHistory: MolecularHistory,
        eventFormatter: (MolecularCharacteristics) -> String,
        extractor: (MolecularCharacteristics) -> ClinicalEvidence?
    ): Set<Pair<String, ClinicalEvidence>> =
        molecularHistory.molecularTests.mapNotNull {
            extractor.invoke(it.characteristics)?.let { e -> eventFormatter.invoke(it.characteristics) to e }
        }.toSet()
}