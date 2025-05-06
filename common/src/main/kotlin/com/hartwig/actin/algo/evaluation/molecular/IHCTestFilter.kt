package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.clinical.IHCTest

object IHCTestFilter {

    private const val PD_L1 = "PD-L1"

    // For lung cancer the measurement type for PD-L1 is assumed to be TPS if not otherwise specified
    fun allPDL1Tests(ihcTests: List<IHCTest>, measureToFind: String? = null, isLungCancer: Boolean? = null): List<IHCTest> {
        val allPDL1Tests = mostRecentOrUnknownDateIhcTests(ihcTests).filter { test -> test.item == PD_L1 }
        return if (measureToFind == null || (measureToFind == "TPS" && isLungCancer == true && allPDL1Tests.all { it.measure == null })) {
            allPDL1Tests
        } else {
            allPDL1Tests.filter { measureToFind == it.measure }
        }
    }

    fun allIHCTestsForProtein(ihcTests: List<IHCTest>, protein: String): List<IHCTest> {
        return mostRecentOrUnknownDateIhcTests(ihcTests).filter { it.item == protein }
    }

    fun mostRecentOrUnknownDateIhcTests(ihcTests: List<IHCTest>): Set<IHCTest> {
        val (withDate, withoutDate) = ihcTests.partition { it.measureDate != null }
        return withDate
            .groupBy { it.item }
            .values
            .flatMap { tests ->
                val mostRecentDate = tests.maxOfOrNull { it.measureDate!! }
                tests.filter { it.measureDate == mostRecentDate }
            }.toSet() + withoutDate.toSet()
    }
}