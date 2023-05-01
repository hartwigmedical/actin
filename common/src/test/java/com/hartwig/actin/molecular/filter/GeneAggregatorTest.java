package com.hartwig.actin.molecular.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.gene.ImmutableKnownGene;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class GeneAggregatorTest {

    private static final String GENE = "gene";
    private static final ImmutableKnownGene ONCO_GENE = gene(GENE, GeneRole.ONCO);
    private static final ImmutableKnownGene UNKNOWN_GENE = gene(GENE, GeneRole.UNKNOWN);

    @Test
    public void shouldUseKnownGeneRolesWhenMultipleRoles() {
        assertThat(GeneAggregator.aggregate(Set.of(ONCO_GENE, UNKNOWN_GENE))).containsOnly(ONCO_GENE);
    }

    @Test
    public void shouldUseUnknownGeneRolesWhenOnlyRole() {
        assertThat(GeneAggregator.aggregate(Set.of(UNKNOWN_GENE))).containsOnly(UNKNOWN_GENE);
    }

    @Test
    public void shouldNotAggregateWhenNotRequired() {
        ImmutableKnownGene anotherGene = ONCO_GENE.withGene("another_gene");
        assertThat(GeneAggregator.aggregate(Set.of(ONCO_GENE, anotherGene))).containsOnly(ONCO_GENE, anotherGene);
    }

    @NotNull
    private static ImmutableKnownGene gene(String name, GeneRole role) {
        return ImmutableKnownGene.builder().gene(name).geneRole(role).build();
    }

}