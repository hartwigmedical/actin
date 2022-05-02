package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.junit.Test;

public class PriorMolecularTestFunctionsTest {

    @Test
    public void canFilterPriorMolecularTestsForPDL1WithSpecificMeasure() {
        PriorMolecularTest test1 = ImmutablePriorMolecularTest.builder().test("Archer").item("PD-L1").build();
        PriorMolecularTest test2 = ImmutablePriorMolecularTest.builder().test("IHC").item("PD-L1").measure("CPS").build();
        PriorMolecularTest test3 = ImmutablePriorMolecularTest.builder().test("IHC").item("PD-L1").measure("wrong").build();
        PriorMolecularTest test4 = ImmutablePriorMolecularTest.builder().test("IHC").item("BRAF").build();

        List<PriorMolecularTest> filtered = PriorMolecularTestFunctions.allPDL1Tests(Lists.newArrayList(test1, test2, test3, test4), "CPS");

        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(test2));
    }

    @Test
    public void canFilterPriorMolecularTestsOnIHCForGene() {
        PriorMolecularTest test1 = ImmutablePriorMolecularTest.builder().test("Archer").item("gene 1").build();
        PriorMolecularTest test2 = ImmutablePriorMolecularTest.builder().test("IHC").item("gene 1").build();
        PriorMolecularTest test3 = ImmutablePriorMolecularTest.builder().test("IHC").item("gene 2").build();

        List<PriorMolecularTest> filtered =
                PriorMolecularTestFunctions.allIHCTestsForGene(Lists.newArrayList(test1, test2, test3), "gene 1");

        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(test2));
    }
}