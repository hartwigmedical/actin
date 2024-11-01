package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.report.interpretation.MolecularCharacteristicFormat

object MolecularClinicalEvidenceFunctions {

    fun molecularEvidenceByEvent(molecularHistory: MolecularHistory): List<Pair<String, ClinicalEvidence>> {
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
                { MolecularCharacteristicFormat.formatTumorMutationalLoad(it) },
                { it.tumorMutationalLoadEvidence })
        val allTMB =
            extractCharacteristics(
                molecularHistory,
                { MolecularCharacteristicFormat.formatTumorMutationalBurden(it) },
                { it.tumorMutationalBurdenEvidence })
        val allHRD =
            extractCharacteristics(
                molecularHistory,
                { "HR ${MolecularCharacteristicFormat.formatHomologuousRepair(it)}" },
                { it.homologousRepairEvidence })
        return allMSI + allTMB + allTML + allHRD + allDrivers.map { it.event to it.evidence }
    }

    private fun extractCharacteristics(
        molecularHistory: MolecularHistory,
        eventFormatter: (MolecularCharacteristics) -> String,
        extractor: (MolecularCharacteristics) -> ClinicalEvidence?
    ): List<Pair<String, ClinicalEvidence>> =
        molecularHistory.molecularTests.mapNotNull {
            extractor.invoke(it.characteristics)?.let { e -> eventFormatter.invoke(it.characteristics) to e }
        }
}