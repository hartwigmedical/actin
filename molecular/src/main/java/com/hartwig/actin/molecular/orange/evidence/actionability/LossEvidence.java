package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

public class LossEvidence implements EvidenceMatcher<PurpleCopyNumber> {

    @NotNull
    private final List<ActionableGene> actionableLosses;

    @NotNull
    public static LossEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> actionableLosses = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableEvents.genes()) {
            if (actionableGene.event() == GeneEvent.DELETION) {
                actionableLosses.add(actionableGene);
            }
        }

        return new LossEvidence(actionableLosses);
    }

    private LossEvidence(@NotNull final List<ActionableGene> actionableLosses) {
        this.actionableLosses = actionableLosses;
    }

    @NotNull
    @Override
    public List<ActionableEvent> findMatches(@NotNull PurpleCopyNumber loss) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableGene actionableLos : actionableLosses) {
            if (actionableLos.gene().equals(loss.gene())) {
                matches.add(actionableLos);
            }
        }
        return matches;
    }
}
