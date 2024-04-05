package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.priorMolecularTest
import com.hartwig.actin.algo.evaluation.molecular.PriorMolecularTestFunctions.allIHCTestsForProtein
import com.hartwig.actin.algo.evaluation.molecular.PriorMolecularTestFunctions.allPDL1Tests
import com.hartwig.actin.algo.evaluation.molecular.PriorMolecularTestFunctions.allPDL1TestsWithSpecificMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorMolecularTestFunctionsTest {

    @Test
    fun `Should filter prior molecular tests for PDL1`() {
        val test1 = priorMolecularTest(test = "Archer", item = "PD-L1")
        val test2 = priorMolecularTest(test = "IHC", item = "PD-L1")
        val test3 = priorMolecularTest(test = "IHC", item = "PD-L1")
        val test4 = priorMolecularTest(test = "IHC", item = "BRAF")
        val filtered = allPDL1Tests(listOf(test1, test2, test3, test4))
        assertThat(filtered).containsExactlyElementsOf(listOf(test2, test3))
    }

    @Test
    fun `Should filter prior molecular tests for PDL1 with specific measure`() {
        val test1 = priorMolecularTest(test = "Archer", item = "PD-L1")
        val test2 = priorMolecularTest(test = "IHC", item = "PD-L1", measure = "CPS")
        val test3 = priorMolecularTest(test = "IHC", item = "PD-L1", measure = "wrong")
        val test4 = priorMolecularTest(test = "IHC", item = "BRAF")
        val filtered = allPDL1TestsWithSpecificMeasurement(listOf(test1, test2, test3, test4), "CPS")
        assertThat(filtered).containsExactly(test2)
    }

    @Test
    fun `Should filter prior molecular tests on IHC for specific protein`() {
        val test1 = priorMolecularTest(test = "Archer", item = "protein 1")
        val test2 = priorMolecularTest(test = "IHC", item = "protein 1")
        val test3 = priorMolecularTest(test = "IHC", item = "protein 2")
        val filtered = allIHCTestsForProtein(listOf(test1, test2, test3), "protein 1")
        assertThat(filtered).containsExactly(test2)
    }
}