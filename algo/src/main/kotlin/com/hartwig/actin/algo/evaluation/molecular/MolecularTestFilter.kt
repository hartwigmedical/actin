package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class MolecularTestFilter(private val maxTestAge: LocalDate? = null) {

    fun filter(tests: List<MolecularTest>): List<MolecularTest> {
        if (tests.isNotEmpty() && maxTestAge != null) {
            val sortedTests = tests.sortedBy { it.date }.reversed()
            val mostRecentTestDate = sortedTests.first().date
            val mostRecentWGS = tests.firstOrNull { it.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME }?.date
            return tests.filter {
                it.date?.let { testDate ->
                    if (it.experimentType == ExperimentType.PANEL && mostRecentWGS != null) {
                        testDate > mostRecentWGS
                    } else {
                        testDate >= mostRecentTestDate || testDate > maxTestAge
                    }
                } ?: true
            }
        }
        return tests
    }
}