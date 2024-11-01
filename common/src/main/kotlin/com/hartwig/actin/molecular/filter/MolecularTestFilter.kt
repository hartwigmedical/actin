package com.hartwig.actin.molecular.filter

import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class MolecularTestFilter(private val maxTestAge: LocalDate? = null) {

    fun apply(tests: List<MolecularTest>): List<MolecularTest> {
        val filteredTests = removeFailedTests(tests)
        if (filteredTests.isNotEmpty() && maxTestAge != null) {
            val sortedTests = filteredTests.sortedBy { it.date }.reversed()
            val mostRecentTestDate = sortedTests.first().date
            val mostRecentOncoAct = filteredTests.firstOrNull { it.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME }?.date
            val mostRecentOncoPanel = filteredTests.firstOrNull { it.experimentType == ExperimentType.HARTWIG_TARGETED }?.date
            return filteredTests.filter {
                it.date?.let { testDate ->
                    when {
                        it.experimentType == ExperimentType.PANEL && mostRecentOncoPanel != null && mostRecentOncoAct == null && it.drivers.fusions.isNotEmpty() -> testDate >= maxTestAge
                        it.experimentType == ExperimentType.PANEL && mostRecentOncoPanel != null -> testDate > mostRecentOncoPanel
                        it.experimentType == ExperimentType.PANEL && mostRecentOncoAct != null -> testDate > mostRecentOncoAct
                        else -> testDate >= mostRecentTestDate || testDate >= maxTestAge
                    }
                } ?: true
            }
        }
        return filteredTests
    }

    private fun removeFailedTests(tests: List<MolecularTest>): List<MolecularTest> {
        return tests.filterNot { (it as? MolecularRecord)?.hasSufficientQuality == false }
    }
}