package com.hartwig.actin.datamodel.molecular

import java.time.LocalDate

class MolecularHistory(
    private val molecularTests: List<MolecularTest>
) {

    fun allOrangeMolecularRecords(): List<MolecularTest> {
        return molecularTests.filter { isHartwigTest(it) }
    }

    fun latestOrangeMolecularRecord(): MolecularTest? {
        return allOrangeMolecularRecords().maxByOrNull { it.date ?: LocalDate.MIN }
    }

    fun allPanels(): List<MolecularTest> {
        return molecularTests.filter { it.experimentType == ExperimentType.PANEL }
    }

    fun allPanelsAndIhc(): List<MolecularTest> {
        return molecularTests.filter { it.experimentType == ExperimentType.PANEL || it.experimentType == ExperimentType.IHC }
    }

    private fun isHartwigTest(test: MolecularTest): Boolean {
        return test.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME ||
                test.experimentType == ExperimentType.HARTWIG_TARGETED
    }

    companion object {
        fun empty(): MolecularHistory {
            return MolecularHistory(emptyList())
        }
    }
}