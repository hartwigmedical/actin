package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.Panel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import java.time.LocalDate

data class MolecularHistory(
    val molecularTests: List<MolecularTest>,
) {
    fun allIHCTests(): List<PriorMolecularTest> {
        return molecularTests.filterIsInstance<IHCMolecularTest>().map { it.test }
    }

    fun allOrangeMolecularRecords(): List<MolecularRecord> {
        return molecularTests.filterIsInstance<MolecularRecord>()
    }

    fun allPanels(): List<Panel> {
        return molecularTests.filterIsInstance<Panel>()
    }

    fun allArcherPanels(): List<ArcherPanel> {
        return molecularTests.filterIsInstance<ArcherPanel>()
    }

    fun allGenericPanels(): List<GenericPanel> {
        return molecularTests.filterIsInstance<GenericPanel>()
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