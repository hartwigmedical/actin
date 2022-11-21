package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.evidence.matching.FusionMatching;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.fusion.ActionableFusion;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;

public class FusionEvidence {

    @NotNull
    private final List<ActionableGene> actionablePromiscuous;
    @NotNull
    private final List<ActionableFusion> actionableFusions;

    @NotNull
    public static FusionEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> actionablePromiscuous = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableEvents.genes()) {
            if (actionableGene.event() == GeneEvent.FUSION || actionableGene.event() == GeneEvent.ACTIVATION
                    || actionableGene.event() == GeneEvent.ANY_MUTATION) {
                actionablePromiscuous.add(actionableGene);
            }
        }

        return new FusionEvidence(actionablePromiscuous, actionableEvents.fusions());
    }

    private FusionEvidence(@NotNull final List<ActionableGene> actionablePromiscuous,
            @NotNull final List<ActionableFusion> actionableFusions) {
        this.actionablePromiscuous = actionablePromiscuous;
        this.actionableFusions = actionableFusions;
    }

    @NotNull
    public List<ActionableEvent> findMatches(@NotNull LinxFusion fusion) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        for (ActionableGene actionable : actionablePromiscuous) {
            if (isPromiscuousMatch(actionable, fusion)) {
                applicableEvents.add(actionable);
            }
        }

        for (ActionableFusion actionable : actionableFusions) {
            if (FusionMatching.isGeneMatch(actionable, fusion) && FusionMatching.isExonMatch(actionable, fusion)) {
                applicableEvents.add(actionable);
            }
        }

        return applicableEvents;
    }

    private static boolean isPromiscuousMatch(@NotNull ActionableGene actionable, @NotNull LinxFusion fusion) {
        if (fusion.type() == FusionType.PROMISCUOUS_3) {
            return actionable.gene().equals(fusion.geneEnd());
        } else if (fusion.type() == FusionType.PROMISCUOUS_5) {
            return actionable.gene().equals(fusion.geneStart());
        } else {
            return actionable.gene().equals(fusion.geneStart()) || actionable.gene().equals(fusion.geneEnd());
        }
    }
}
