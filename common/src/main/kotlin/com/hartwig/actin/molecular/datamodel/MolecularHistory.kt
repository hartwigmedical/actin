package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import java.time.LocalDate

data class MolecularHistory(
    val molecularTests: List<MolecularTest<*>>
) {
    fun allIHCTests(): List<PriorMolecularTest> {
        return molecularTests.filterIsInstance<IHCMolecularTest>().map { it.test }
    }

    fun allOrangeMolecularRecords(): List<MolecularRecord> {
        return molecularTests.filterIsInstance<MolecularRecord>()
    }

    fun allPanels(): List<PanelRecord> {
        return molecularTests.filterIsInstance<PanelRecord>()
    }

    fun allArcherPanels(): List<PanelRecord> {
        return molecularTests.filter { it.type == ExperimentType.ARCHER }.filterIsInstance<PanelRecord>()
    }

    fun allGenericPanels(): List<PanelRecord> {
        return molecularTests.filter { it.type == ExperimentType.GENERIC_PANEL }.filterIsInstance<PanelRecord>()
    }

    fun allOtherTests(): List<OtherPriorMolecularTest> {
        return molecularTests.filterIsInstance<OtherPriorMolecularTest>()
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
    }
}