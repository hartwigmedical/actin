package com.hartwig.actin.molecular.filter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class KnownGeneFileTest {

    private static final String EXAMPLE_TSV = Resources.getResource("known_genes/example_known_genes.tsv").getPath();

    @Test
    public void canCreateFromExampleTsv() throws IOException {
        List<KnownGene> knownGenes = KnownGeneFile.read(EXAMPLE_TSV);

        assertEquals(2, knownGenes.size());

        KnownGene geneA = findByGene(knownGenes, "gene A");
        assertEquals(GeneRole.ONCO, geneA.geneRole());

        KnownGene geneB = findByGene(knownGenes, "gene B");
        assertEquals(GeneRole.UNKNOWN, geneB.geneRole());
    }

    @NotNull
    private static KnownGene findByGene(@NotNull List<KnownGene> knownGenes, @NotNull String geneToFind) {
        for (KnownGene knownGene : knownGenes) {
            if (knownGene.gene().equals(geneToFind)) {
                return knownGene;
            }
        }

        throw new IllegalStateException("Could not find known gene for " + geneToFind);
    }
}