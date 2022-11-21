package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

public class CopyNumberEvidence {

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
    public List<ActionableEvent> findMatches(@NotNull PurpleCopyNumber copyNumber) {
        if (copyNumber.interpretation().isGain()) {
            return findGeneMatches(actionableAmplifications, copyNumber);
        } else if (copyNumber.interpretation().isLoss()) {
            return findGeneMatches(actionableLosses, copyNumber);
        } else {
            return Lists.newArrayList();
        }
    }

    @NotNull
    private static List<ActionableEvent> findGeneMatches(@NotNull List<ActionableGene> actionableGenes,
            @NotNull PurpleCopyNumber copyNumber) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableGenes) {
            if (actionableGene.gene().equals(copyNumber.gene())) {
                matches.add(actionableGene);
            }
        }
        return matches;
    }
}
