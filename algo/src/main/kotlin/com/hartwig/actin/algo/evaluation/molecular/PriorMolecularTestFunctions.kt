package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

internal object PriorMolecularTestFunctions {

    private const val PD_L1 = "PD-L1"
    private const val IHC = "IHC"

    fun allPDL1Tests(priorMolecularTests: List<PriorMolecularTest>): List<PriorMolecularTest> {
        return allIHCTests(priorMolecularTests).filter { it.item == PD_L1 }
    }

    fun allPDL1TestsWithSpecificMeasurement(priorMolecularTests: List<PriorMolecularTest>, measureToFind: String): List<PriorMolecularTest> {
        return allPDL1Tests(priorMolecularTests).filter { measureToFind == it.measure }
    }

    fun allIHCTestsForProtein(priorMolecularTests: List<PriorMolecularTest>, protein: String): List<PriorMolecularTest> {
        return allIHCTests(priorMolecularTests).filter { it.item == protein }
    }

    private fun allIHCTests(priorMolecularTests: List<PriorMolecularTest>): List<PriorMolecularTest> {
        return priorMolecularTests.filter { it.test == IHC }
    }
}