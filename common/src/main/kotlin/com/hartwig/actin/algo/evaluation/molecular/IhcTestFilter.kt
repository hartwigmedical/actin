package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.datamodel.clinical.IHC_TEST_TYPE

object IhcTestFilter {

    private const val PD_L1 = "PD-L1"

    // For lung cancer the measurement type for PD-L1 is assumed to be TPS if not otherwise specified
    fun allPDL1Tests(
        molecularTests: List<IHCTest>, measureToFind: String? = null, isLungCancer: Boolean? = null
    ): List<IHCTest> {
        val allPDL1Tests = mostRecentOrUnknownDateIhcTests(molecularTests).filter { test -> test.item == PD_L1 }
        return if (measureToFind == null || (measureToFind == "TPS" && isLungCancer == true && allPDL1Tests.all { it.measure == null })) {
            allPDL1Tests
        } else {
            allPDL1Tests.filter { measureToFind == it.measure }
        }
    }

    fun allIHCTestsForProtein(molecularTests: List<IHCTest>, protein: String): List<IHCTest> {
        return mostRecentOrUnknownDateIhcTests(molecularTests).filter { it.item == protein }
    }

    fun mostRecentOrUnknownDateIhcTests(molecularTests: List<IHCTest>): Set<IHCTest> {
        val (withDate, withoutDate) = molecularTests.filter { it.test == IHC_TEST_TYPE }.partition { it.measureDate != null }
        val mostRecentPerItem = withDate.groupBy { it.item }.flatMap { (_, tests) ->
            val mostRecentDate = tests.maxOfOrNull { it.measureDate!! }
            tests.filter { it.measureDate == mostRecentDate }
        }.toSet()

        return mostRecentPerItem + withoutDate.toSet()
    }
}