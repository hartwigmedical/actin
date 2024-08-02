package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.clinical.datamodel.IHC_TEST_TYPE
import com.hartwig.actin.clinical.datamodel.PriorIHCTest

internal object PriorIHCTestFunctions {

    private const val PD_L1 = "PD-L1"

    // For lung cancer the measurement type for PD-L1 is assumed to be TPS if not otherwise specified
    fun allPDL1Tests(
        priorMolecularTests: List<PriorIHCTest>, measureToFind: String? = null, isLungCancer: Boolean? = null
    ): List<PriorIHCTest> {
        val allPDL1Tests = allIHCTests(priorMolecularTests).filter { test -> test.item == PD_L1 }
        return if (measureToFind == null || (measureToFind == "TPS" && isLungCancer == true && allPDL1Tests.all { it.measure == null })) {
            allPDL1Tests
        } else {
            allPDL1Tests.filter { measureToFind == it.measure }
        }
    }

    fun allIHCTestsForProtein(priorMolecularTests: List<PriorIHCTest>, protein: String): List<PriorIHCTest> {
        return allIHCTests(priorMolecularTests).filter { it.item == protein }
    }

    private fun allIHCTests(priorMolecularTests: List<PriorIHCTest>): List<PriorIHCTest> {
        return priorMolecularTests.filter { it.test == IHC_TEST_TYPE }
    }
}