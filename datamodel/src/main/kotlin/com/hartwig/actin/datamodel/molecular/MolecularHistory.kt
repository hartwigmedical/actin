package com.hartwig.actin.datamodel.molecular

import java.time.LocalDate

data class MolecularHistory(
    val molecularTests: List<MolecularTest>,
    val maxTestAge: LocalDate? = null
) {
    fun allOrangeMolecularRecords(): List<MolecularRecord> {
        return molecularTests.filterIsInstance<MolecularRecord>()
    }

    fun allPanels(): List<PanelRecord> {
        return molecularTests.filterIsInstance<PanelRecord>()
    }

    fun latestOrangeMolecularRecord(): MolecularRecord? {
        return allOrangeMolecularRecords()
            .maxByOrNull { it.date ?: LocalDate.MIN }
    }

    fun hasMolecularData(): Boolean {
        return molecularTests.isNotEmpty()
    }

    fun molecularTestsForTrialMatching() =
        molecularTests.filter { it.date?.let { date -> maxTestAge == null || date >= maxTestAge } ?: true }

    companion object {
        fun empty(): MolecularHistory {
            return MolecularHistory(emptyList())
        }
    }
}