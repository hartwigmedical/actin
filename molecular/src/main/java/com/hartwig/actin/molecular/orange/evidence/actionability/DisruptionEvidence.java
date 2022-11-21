package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

class DisruptionEvidence implements EvidenceMatcher<LinxDisruption> {

    @NotNull
    private final List<ActionableGene> applicableActionableGenes;

    @NotNull
    public static DisruptionEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> applicableActionableGenes = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableEvents.genes()) {
            if (actionableGene.event() == GeneEvent.ANY_MUTATION) {
                applicableActionableGenes.add(actionableGene);
            }
        }
        return new DisruptionEvidence(applicableActionableGenes);
    }

    private DisruptionEvidence(@NotNull final List<ActionableGene> applicableActionableGenes) {
        this.applicableActionableGenes = applicableActionableGenes;
    }

    @NotNull
    @Override
    public List<ActionableEvent> findMatches(@NotNull LinxDisruption disruption) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableGene actionableGene : applicableActionableGenes) {
            if (actionableGene.gene().equals(disruption.gene())) {
                matches.add(actionableGene);
            }
        }

        return matches;
    }
}
