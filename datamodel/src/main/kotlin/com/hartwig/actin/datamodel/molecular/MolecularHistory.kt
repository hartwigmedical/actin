package com.hartwig.actin.datamodel.molecular

import java.time.LocalDate

data class MolecularHistory(
    val molecularTests: List<MolecularTest>
) {

    fun allOrangeMolecularRecords(): List<MolecularRecord> {
        return molecularTests.filterIsInstance<MolecularRecord>()
    }

    fun allPanels(): List<PanelRecord> {
        return molecularTests.filterIsInstance<PanelRecord>()
    }

    fun latestOrangeMolecularRecord(): MolecularRecord? {
        return allOrangeMolecularRecords().maxByOrNull { it.date ?: LocalDate.MIN }
    }

    companion object {
        fun empty(): MolecularHistory {
            return MolecularHistory(emptyList())
        }
    }
}