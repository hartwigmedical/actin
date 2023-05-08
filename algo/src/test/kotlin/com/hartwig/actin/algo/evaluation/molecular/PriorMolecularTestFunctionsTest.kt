package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.molecular.PriorMolecularTestFunctions.allIHCTestsForProtein
import com.hartwig.actin.algo.evaluation.molecular.PriorMolecularTestFunctions.allPDL1Tests
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.junit.Assert
import org.junit.Test

class PriorMolecularTestFunctionsTest {
    @Test
    fun canFilterPriorMolecularTestsForPDL1WithSpecificMeasure() {
        val test1: PriorMolecularTest = MolecularTestFactory.priorBuilder().test("Archer").item("PD-L1").build()
        val test2: PriorMolecularTest = MolecularTestFactory.priorBuilder().test("IHC").item("PD-L1").measure("CPS").build()
        val test3: PriorMolecularTest = MolecularTestFactory.priorBuilder().test("IHC").item("PD-L1").measure("wrong").build()
        val test4: PriorMolecularTest = MolecularTestFactory.priorBuilder().test("IHC").item("BRAF").build()
        val filtered = allPDL1Tests(listOf(test1, test2, test3, test4), "CPS")
        Assert.assertEquals(1, filtered.size.toLong())
        Assert.assertTrue(filtered.contains(test2))
    }

    @Test
    fun canFilterPriorMolecularTestsOnIHCForProtein() {
        val test1: PriorMolecularTest = MolecularTestFactory.priorBuilder().test("Archer").item("protein 1").build()
        val test2: PriorMolecularTest = MolecularTestFactory.priorBuilder().test("IHC").item("protein 1").build()
        val test3: PriorMolecularTest = MolecularTestFactory.priorBuilder().test("IHC").item("protein 2").build()
        val filtered = allIHCTestsForProtein(listOf(test1, test2, test3), "protein 1")
        Assert.assertEquals(1, filtered.size.toLong())
        Assert.assertTrue(filtered.contains(test2))
    }
}