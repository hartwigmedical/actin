package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.junit.Test;

public class PriorMolecularTestFunctionsTest {

    @Test
    public void canFilterPriorMolecularTestsForPDL1WithSpecificMeasure() {
        PriorMolecularTest test1 = MolecularTestFactory.priorBuilder().test("Archer").item("PD-L1").build();
        PriorMolecularTest test2 = MolecularTestFactory.priorBuilder().test("IHC").item("PD-L1").measure("CPS").build();
        PriorMolecularTest test3 = MolecularTestFactory.priorBuilder().test("IHC").item("PD-L1").measure("wrong").build();
        PriorMolecularTest test4 = MolecularTestFactory.priorBuilder().test("IHC").item("BRAF").build();

        List<PriorMolecularTest> filtered = PriorMolecularTestFunctions.allPDL1Tests(Lists.newArrayList(test1, test2, test3, test4), "CPS");

        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(test2));
    }

    @Test
    public void canFilterPriorMolecularTestsOnIHCForProtein() {
        PriorMolecularTest test1 = MolecularTestFactory.priorBuilder().test("Archer").item("protein 1").build();
        PriorMolecularTest test2 = MolecularTestFactory.priorBuilder().test("IHC").item("protein 1").build();
        PriorMolecularTest test3 = MolecularTestFactory.priorBuilder().test("IHC").item("protein 2").build();

        List<PriorMolecularTest> filtered =
                PriorMolecularTestFunctions.allIHCTestsForProtein(Lists.newArrayList(test1, test2, test3), "protein 1");

        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(test2));
    }
}