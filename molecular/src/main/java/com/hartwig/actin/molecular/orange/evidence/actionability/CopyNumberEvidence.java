package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

class CopyNumberEvidence implements EvidenceMatcher<PurpleGainLoss>{

    @NotNull
    private final List<ActionableGene> actionableAmplifications;
    @NotNull
    private final List<ActionableGene> actionableLosses;

    @NotNull
    public static CopyNumberEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> actionableAmplifications = Lists.newArrayList();
        List<ActionableGene> actionableLosses = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableEvents.genes()) {
            if (actionableGene.event() == GeneEvent.AMPLIFICATION) {
                actionableAmplifications.add(actionableGene);
            } else if (actionableGene.event() == GeneEvent.DELETION) {
                actionableLosses.add(actionableGene);
            }
        }
        return new CopyNumberEvidence(actionableAmplifications, actionableLosses);
    }

    private CopyNumberEvidence(@NotNull final List<ActionableGene> actionableAmplifications,
            @NotNull final List<ActionableGene> actionableLosses) {
        this.actionableAmplifications = actionableAmplifications;
        this.actionableLosses = actionableLosses;
    }

    @NotNull
    @Override
    public List<ActionableEvent> findMatches(@NotNull PurpleGainLoss gainLoss) {
        switch (gainLoss.interpretation()) {
            case FULL_GAIN:
            case PARTIAL_GAIN: {
                return findMatches(gainLoss, actionableAmplifications);
            }
            case FULL_LOSS:
            case PARTIAL_LOSS: {
                return findMatches(gainLoss, actionableLosses);
            }
            default: {
                return Lists.newArrayList();
            }
        }
    }

    @NotNull
    private static List<ActionableEvent> findMatches(@NotNull PurpleGainLoss gainLoss, @NotNull List<ActionableGene> actionableEvents) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableGene actionableEvent : actionableEvents) {
            if (actionableEvent.gene().equals(gainLoss.gene())) {
                matches.add(actionableEvent);
            }
        }
        return matches;
    }
}
