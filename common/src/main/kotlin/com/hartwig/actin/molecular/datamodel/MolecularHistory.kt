package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import java.time.LocalDate

data class MolecularHistory(
    val molecularTests: List<MolecularTest<*>>,
) {
    fun allIHCTests(): List<PriorMolecularTest> {
        return molecularTests.filter { it.type == ExperimentType.IHC }
            .map { it.result as PriorMolecularTest }
    }

    fun allOrangeMolecularRecords(): List<MolecularRecord> {
        return molecularTests.filter { it.type == ExperimentType.WHOLE_GENOME || it.type == ExperimentType.TARGETED }
            .map { it.result as MolecularRecord }
    }

    fun allPanels(): List<com.hartwig.actin.molecular.datamodel.panel.Panel> {
        return listOf(allArcherPanels() + allGenericPanels()).flatten()
    }

    fun allArcherPanels(): List<ArcherPanel> {
        return molecularTests.filter { it.type == ExperimentType.ARCHER }
            .map { it.result as ArcherPanel }
    }

    fun allGenericPanels(): List<GenericPanel> {
        return molecularTests.filter { it.type == ExperimentType.GENERIC_PANEL }
            .map { it.result as GenericPanel }
    }

    fun latestOrangeMolecularRecord(): MolecularRecord? {
        return allOrangeMolecularRecords()
            .maxByOrNull { it.date ?: LocalDate.MIN }
    }

    fun hasMolecularData(): Boolean {
        return molecularTests.isNotEmpty()
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