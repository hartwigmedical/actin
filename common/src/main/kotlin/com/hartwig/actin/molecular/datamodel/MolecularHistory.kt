package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import java.time.LocalDate

data class MolecularHistory(
    val molecularTests: List<MolecularTest>
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

    fun allArcherPanels(): List<ArcherPanelExtraction> {
        return molecularTests.filterIsInstance<PanelRecord>().mapNotNull { it.archerPanelExtraction }
    }

    fun allGenericPanels(): List<GenericPanelExtraction> {
        return molecularTests.filterIsInstance<PanelRecord>().mapNotNull { it.genericPanelExtraction }
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