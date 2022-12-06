package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

class AmplificationEvidence implements EvidenceMatcher<PurpleCopyNumber> {

    @NotNull
    private final List<ActionableGene> actionableAmplifications;

    @NotNull
    public static AmplificationEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> actionableAmplifications = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableEvents.genes()) {
            if (actionableGene.event() == GeneEvent.AMPLIFICATION) {
                actionableAmplifications.add(actionableGene);
            }
        }

        return new AmplificationEvidence(actionableAmplifications);
    }

    private AmplificationEvidence(@NotNull final List<ActionableGene> actionableAmplifications) {
        this.actionableAmplifications = actionableAmplifications;
    }

    @NotNull
    @Override
    public List<ActionableEvent> findMatches(@NotNull PurpleCopyNumber amp) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableGene actionableAmp : actionableAmplifications) {
            if (actionableAmp.gene().equals(amp.gene())) {
                matches.add(actionableAmp);
            }
        }
        return matches;
    }
}
