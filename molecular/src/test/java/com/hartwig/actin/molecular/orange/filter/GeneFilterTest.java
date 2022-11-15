package com.hartwig.actin.molecular.orange.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import org.junit.Test;

public class GeneFilterTest {

    @Test
    public void canFilterGenes() {
        GeneFilter filter = new GeneFilter(Sets.newHashSet("gene A"));

        assertEquals(1, filter.size());

        assertTrue(filter.include("gene A"));
        assertFalse(filter.include("gene B"));
    }
}