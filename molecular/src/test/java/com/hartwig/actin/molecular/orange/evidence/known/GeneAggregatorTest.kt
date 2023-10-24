package com.hartwig.actin.molecular.orange.evidence.known;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.gene.ImmutableKnownGene;
import com.hartwig.serve.datamodel.gene.KnownGene;

import org.junit.Test;

public class GeneAggregatorTest {

    private static final String GENE = "gene";
    private static final KnownGene ONCO_GENE = gene(GeneRole.ONCO);
    private static final KnownGene BOTH_GENE = gene(GeneRole.BOTH);
    private static final KnownGene UNKNOWN_GENE = gene(GeneRole.UNKNOWN);

    @Test
    public void shouldUseBothGeneRolesWhenOncoBothAndUnknownInSet() {
        assertThat(GeneAggregator.aggregate(Set.of(ONCO_GENE, BOTH_GENE, UNKNOWN_GENE))).containsOnly(BOTH_GENE);
    }

    @Test
    public void shouldUseOncoGeneRolesWhenOncoAndUnknownInSet() {
        assertThat(GeneAggregator.aggregate(Set.of(ONCO_GENE, UNKNOWN_GENE))).containsOnly(ONCO_GENE);
    }

    @Test
    public void shouldUseUnknownGeneRolesWhenOnlyRole() {
        assertThat(GeneAggregator.aggregate(Set.of(UNKNOWN_GENE))).containsOnly(UNKNOWN_GENE);
    }

    @Test
    public void shouldNotAggregateWhenNotRequired() {
        ImmutableKnownGene anotherGene = ImmutableKnownGene.copyOf(ONCO_GENE).withGene("another_gene");
        assertThat(GeneAggregator.aggregate(Set.of(ONCO_GENE, anotherGene))).containsOnly(ONCO_GENE, anotherGene);
    }

    private static KnownGene gene(GeneRole role) {
        return ImmutableKnownGene.builder().gene(GeneAggregatorTest.GENE).geneRole(role).build();
    }
}