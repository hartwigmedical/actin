package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

class HomozygousDisruptionEvidence implements EvidenceMatcher<LinxHomozygousDisruption> {

    @NotNull
    private final List<ActionableGene> actionableGenes;

    @NotNull
    public static HomozygousDisruptionEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> actionableGenes = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableEvents.genes()) {
            if (actionableGene.event() == GeneEvent.DELETION || actionableGene.event() == GeneEvent.ANY_MUTATION
                    || actionableGene.event() == GeneEvent.INACTIVATION) {
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
