package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

class HomozygousDisruptionEvidence implements EvidenceMatcher<LinxHomozygousDisruption> {

    private static final Set<GeneEvent> APPLICABLE_GENE_EVENTS =
            Sets.newHashSet(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION);

    @NotNull
    private final List<ActionableGene> actionableGenes;

    @NotNull
    public static HomozygousDisruptionEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> actionableGenes = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableEvents.genes()) {
            if (APPLICABLE_GENE_EVENTS.contains(actionableGene.event())) {
                actionableGenes.add(actionableGene);
            }
        }

        return new HomozygousDisruptionEvidence(actionableGenes);
    }

    private HomozygousDisruptionEvidence(@NotNull final List<ActionableGene> actionableGenes) {
        this.actionableGenes = actionableGenes;
    }

    @NotNull
    @Override
    public List<ActionableEvent> findMatches(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        List<ActionableEvent> matches = Lists.newArrayList();

        for (ActionableGene actionableGene : actionableGenes) {
            if (actionableGene.gene().equals(homozygousDisruption.gene())) {
                matches.add(actionableGene);
            }
        }

        return matches;
    }
}
