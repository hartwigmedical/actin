package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class PriorMolecularTestComparatorTest {

    @Test
    public void canSortPriorMolecularTests() {
        PriorMolecularTest test1 = withItem("ZZZ");
        PriorMolecularTest test2 = withItem("TP53");
        PriorMolecularTest test3 = withItem("ZZZ");
        PriorMolecularTest test4 = withItem("CK20");

        List<PriorMolecularTest> sorted = Lists.newArrayList(test1, test2, test3, test4);
        sorted.sort(new PriorMolecularTestComparator());

        assertEquals(test4, sorted.get(0));
        assertEquals(test2, sorted.get(1));
        assertEquals("ZZZ", sorted.get(2).item());
        assertEquals("ZZZ", sorted.get(3).item());
    }

    @NotNull
    private static PriorMolecularTest withItem(@NotNull String item) {
        return ImmutablePriorMolecularTest.builder().item(item).test(Strings.EMPTY).build();
    }
}