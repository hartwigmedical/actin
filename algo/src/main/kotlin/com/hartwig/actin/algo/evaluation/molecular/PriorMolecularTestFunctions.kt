package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

internal object PriorMolecularTestFunctions {

    private const val PD_L1 = "PD-L1"
    private const val IHC = "IHC"

    // For lung cancer the measurement type for PD-L1 is assumed to be TPS if not otherwise specified
    fun allPDL1Tests(
        priorMolecularTests: List<PriorMolecularTest>, measureToFind: String? = null, isLungCancer: Boolean? = null
    ): List<PriorMolecularTest> {
        val allPDL1Tests = allIHCTests(priorMolecularTests).filter { test -> test.item == PD_L1 }
        return if (measureToFind == null || measureToFind == "TPS" && isLungCancer == true && allPDL1Tests.all { it.measure == null }) {
            allPDL1Tests
        } else {
            allPDL1Tests.filter { measureToFind == it.measure }
        }
    }

    fun allIHCTestsForProtein(priorMolecularTests: List<PriorMolecularTest>, protein: String): List<PriorMolecularTest> {
        return allIHCTests(priorMolecularTests).filter { it.item == protein }
    }

    private fun allIHCTests(priorMolecularTests: List<PriorMolecularTest>): List<PriorMolecularTest> {
        return priorMolecularTests.filter { it.test == IHC }
    }
}