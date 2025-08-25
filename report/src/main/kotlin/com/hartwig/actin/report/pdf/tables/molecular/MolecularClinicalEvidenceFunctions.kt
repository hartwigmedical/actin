package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.report.interpretation.MolecularCharacteristicFormat

object MolecularClinicalEvidenceFunctions {

    fun molecularEvidenceByEvent(molecularTests: List<MolecularTest>): Set<Pair<String, ClinicalEvidence>> {
        val allDrivers =
            DriverTableFunctions.allDrivers(molecularTests).flatMap { it.second }.toSortedSet(Comparator.comparing { it.event })
        val allMSI =
            extractCharacteristics(
                molecularTests,
                { "MS ${MolecularCharacteristicFormat.formatMicrosatelliteStability(it)}" },
                { it.microsatelliteStability?.evidence })
        val allTML =
            extractCharacteristics(
                molecularTests,
                { MolecularCharacteristicFormat.formatTumorMutationalLoad(it, false) },
                { it.tumorMutationalLoad?.evidence })
        val allTMB =
            extractCharacteristics(
                molecularTests,
                { MolecularCharacteristicFormat.formatTumorMutationalBurden(it, false) },
                { it.tumorMutationalBurden?.evidence })
        val allHRD =
            extractCharacteristics(
                molecularTests,
                { "HR ${MolecularCharacteristicFormat.formatHomologousRecombination(it, false)}" },
                { it.homologousRecombination?.evidence })
        return allMSI + allTMB + allTML + allHRD + allDrivers.map { it.event to it.evidence }
    }

    private fun extractCharacteristics(
        molecularTests: List<MolecularTest>,
        eventFormatter: (MolecularCharacteristics) -> String,
        extractor: (MolecularCharacteristics) -> ClinicalEvidence?
    ): Set<Pair<String, ClinicalEvidence>> =
        molecularTests.mapNotNull {
            extractor.invoke(it.characteristics)?.let { e -> eventFormatter.invoke(it.characteristics) to e }
        }.toSet()
}