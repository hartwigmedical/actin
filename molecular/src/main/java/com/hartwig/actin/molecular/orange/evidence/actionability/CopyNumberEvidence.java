package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

class CopyNumberEvidence implements EvidenceMatcher<PurpleCopyNumber>{

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
    public List<ActionableEvent> findMatches(@NotNull PurpleCopyNumber copyNumber) {
        switch (copyNumber.interpretation()) {
            case FULL_GAIN:
            case PARTIAL_GAIN: {
                return findMatches(copyNumber, actionableAmplifications);
            }
            case FULL_LOSS:
            case PARTIAL_LOSS: {
                return findMatches(copyNumber, actionableLosses);
            }
            default: {
                return Lists.newArrayList();
            }
        }
    }

    @NotNull
    private static List<ActionableEvent> findMatches(@NotNull PurpleCopyNumber copyNumber, @NotNull List<ActionableGene> actionableEvents) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableGene actionableEvent : actionableEvents) {
            if (actionableEvent.gene().equals(copyNumber.gene())) {
                matches.add(actionableEvent);
            }
        }
        return matches;
    }
}
