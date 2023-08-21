package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class PriorMolecularTestKeyComparatorTest {

    @Test
    public void canSortPriorMolecularTestKeys() {
        PriorMolecularTestKey key1 = create("text 1", "test 1");
        PriorMolecularTestKey key2 = create("text 1", "test 2");
        PriorMolecularTestKey key3 = create("text 2", "test 1");

        List<PriorMolecularTestKey> keys = Lists.newArrayList(key2, key3, key1);
        keys.sort(new PriorMolecularTestKeyComparator());

        assertEquals(key1, keys.get(0));
        assertEquals(key2, keys.get(1));
        assertEquals(key3, keys.get(2));
    }

    @NotNull
    private PriorMolecularTestKey create(@NotNull String scoreText, @NotNull String test) {
        return ImmutablePriorMolecularTestKey.builder().scoreText(scoreText).test(test).build();
    }
}