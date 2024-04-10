package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.archer.ArcherPanel
import java.time.LocalDate

data class MolecularHistory(
    val molecularTests: List<MolecularTest<*>>,
) {
    fun allIHCTests(): List<PriorMolecularTest> {
        return molecularTests.filter { it.type == ExperimentType.IHC }
            .map { it.result as PriorMolecularTest }
    }

    fun allMolecularRecords(): List<MolecularRecord> {
        return molecularTests.filter { it.type == ExperimentType.WHOLE_GENOME || it.type == ExperimentType.TARGETED }
            .map { it.result as MolecularRecord }
    }

    fun allArcherPanels(): List<ArcherPanel> {
        return molecularTests.filter { it.type == ExperimentType.ARCHER }
            .map { it.result as ArcherPanel }
    }

    fun latestArcherPanel(): ArcherPanel? {
        return allArcherPanels()
            .maxByOrNull { it.date ?: LocalDate.MIN }
    }

    fun latestMolecularRecord(): MolecularRecord? {
        return allMolecularRecords()
            .maxByOrNull { it.date ?: LocalDate.MIN }
    }

    companion object {
        fun empty(): MolecularHistory {
            return MolecularHistory(emptyList())
        }

        fun fromInputs(molecularRecords: List<MolecularRecord>, priorMolecularTests: List<PriorMolecularTest>): MolecularHistory {
            return MolecularHistory(
                molecularRecords.map { WGSMolecularTest.fromMolecularRecord(it) } +
                        MolecularTestFactory.fromPriorMolecular(priorMolecularTests)
            )
        }
    }
}