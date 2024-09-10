package com.hartwig.actin.molecular.filter

import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class MolecularTestFilter(private val maxTestAge: LocalDate? = null) {

    fun apply(tests: List<MolecularTest>): List<MolecularTest> {
        if (tests.isNotEmpty() && maxTestAge != null) {
            val sortedTests = tests.sortedBy { it.date }.reversed()
            val mostRecentTestDate = sortedTests.first().date
            val mostRecentOncoAct = tests.firstOrNull { it.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME }?.date
            val mostRecentOncoPanel = tests.firstOrNull { it.experimentType == ExperimentType.HARTWIG_TARGETED }?.date
            return tests.filter {
                it.date?.let { testDate ->
                    when {
                        it.experimentType == ExperimentType.PANEL && mostRecentOncoPanel != null && mostRecentOncoAct == null && it.drivers.fusions.isNotEmpty() && testDate >= maxTestAge -> true
                        it.experimentType == ExperimentType.PANEL && mostRecentOncoPanel != null -> testDate > mostRecentOncoPanel
                        it.experimentType == ExperimentType.PANEL && mostRecentOncoAct != null -> testDate > mostRecentOncoAct
                        else -> testDate >= mostRecentTestDate || testDate >= maxTestAge
                    }
                } ?: true
            }
        }
        return tests
    }
}