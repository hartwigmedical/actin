package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

public class HomozygousDisruptionEvidence {

    @NotNull
    private final List<ActionableGene> actionableLosses;

    @NotNull
    public static HomozygousDisruptionEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> actionableLosses = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableEvents.genes()) {
            if (actionableGene.event() == GeneEvent.DELETION) {
                actionableLosses.add(actionableGene);
            }
        }

        return new HomozygousDisruptionEvidence(actionableLosses);
    }

    private HomozygousDisruptionEvidence(@NotNull final List<ActionableGene> actionableLosses) {
        this.actionableLosses = actionableLosses;
    }

    @NotNull
    public List<ActionableEvent> findMatches(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        for (ActionableGene actionableGene : actionableLosses) {
            if (actionableGene.gene().equals(homozygousDisruption.gene())) {
                applicableEvents.add(actionableGene);
            }
        }

        return applicableEvents;
    }
}
