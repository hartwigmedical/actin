package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

internal object PriorMolecularTestFunctions {

    private const val PD_L1 = "PD-L1"
    private const val IHC = "IHC"

    fun allPDL1Tests(priorMolecularTests: List<PriorMolecularTest>): List<PriorMolecularTest> {
        return allIHCTests(priorMolecularTests).filter { it.item == PD_L1 }
    }

    // For lung cancer the measurement type for PD-L1 is assumed to be TPS if not otherwise specified
    fun allPDL1TestsWithSpecificMeasurement(
        priorMolecularTests: List<PriorMolecularTest>,
        measureToFind: String,
        doids: Set<String>? = null
    ): List<PriorMolecularTest> {
        return if (measureToFind == "TPS" && doids?.any { it in DoidConstants.LUNG_NON_SMALL_CELL_CANCER_DOID_SET } == true
            && allPDL1Tests(priorMolecularTests).all { it.measure == null }) {
            allPDL1Tests(priorMolecularTests)
        } else {
            allPDL1Tests(priorMolecularTests).filter { measureToFind == it.measure }
        }
    }

    fun allIHCTestsForProtein(priorMolecularTests: List<PriorMolecularTest>, protein: String): List<PriorMolecularTest> {
        return allIHCTests(priorMolecularTests).filter { it.item == protein }
    }

    private fun allIHCTests(priorMolecularTests: List<PriorMolecularTest>): List<PriorMolecularTest> {
        return priorMolecularTests.filter { it.test == IHC }
    }
}