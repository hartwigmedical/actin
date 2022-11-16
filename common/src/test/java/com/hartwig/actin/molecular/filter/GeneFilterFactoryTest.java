package com.hartwig.actin.molecular.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class GeneFilterFactoryTest {

    private static final String EXAMPLE_TSV = Resources.getResource("filter/gene_filter.tsv").getPath();

    @Test
    public void canCreateFromExampleTsv() throws IOException {
        GeneFilter filter = GeneFilterFactory.createFromTsv(EXAMPLE_TSV);

        assertEquals(2, filter.allowedGenes().size());
        assertTrue(filter.allowedGenes().contains("gene A"));
        assertTrue(filter.allowedGenes().contains("gene B"));
    }
}