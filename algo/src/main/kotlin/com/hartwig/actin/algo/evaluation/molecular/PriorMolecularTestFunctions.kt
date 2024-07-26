package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.clinical.datamodel.PriorIHCTest

internal object PriorMolecularTestFunctions {

    private const val PD_L1 = "PD-L1"

    // For lung cancer the measurement type for PD-L1 is assumed to be TPS if not otherwise specified
    fun allPDL1Tests(
        priorIHCTests: List<PriorIHCTest>, measureToFind: String? = null, isLungCancer: Boolean? = null
    ): List<PriorIHCTest> {
        val allPDL1Tests = priorIHCTests.filter { test -> test.item == PD_L1 }
        return if (measureToFind == null || measureToFind == "TPS" && isLungCancer == true && allPDL1Tests.all { it.measure == null }) {
            allPDL1Tests
        } else {
            allPDL1Tests.filter { measureToFind == it.measure }
        }
    }

    fun allIHCTestsForProtein(priorIHCTests: List<PriorIHCTest>, protein: String): List<PriorIHCTest> {
        return priorIHCTests.filter { it.item == protein }
    }

}