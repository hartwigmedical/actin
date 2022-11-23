package com.hartwig.actin.molecular.orange.evidence.curation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot;
import com.hartwig.serve.datamodel.range.ActionableRange;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class ApplicabilityFiltering {

    private static final Logger LOGGER = LogManager.getLogger(ApplicabilityFiltering.class);

    static final Set<String> NON_APPLICABLE_GENES = Sets.newHashSet();
    static final Set<String> NON_APPLICABLE_AMPLIFICATIONS = Sets.newHashSet();

    static {
        NON_APPLICABLE_GENES.add("CDKN2A");
        NON_APPLICABLE_GENES.add("TP53");

        NON_APPLICABLE_AMPLIFICATIONS.add("VEGFA");
    }

    private ApplicabilityFiltering() {
    }

    public static boolean isApplicable(@NotNull ActionableHotspot actionableHotspot) {
        return eventIsApplicable(actionableHotspot.gene(), actionableHotspot);
    }

    public static boolean isApplicable(@NotNull ActionableRange actionableRange) {
        return eventIsApplicable(actionableRange.gene(), actionableRange);
    }

    public static boolean isApplicable(@NotNull ActionableGene actionableGene) {
        if (actionableGene.event() == GeneEvent.AMPLIFICATION) {
            for (String nonApplicableGene : NON_APPLICABLE_AMPLIFICATIONS) {
                if (actionableGene.gene().equals(nonApplicableGene)) {
                    LOGGER.debug("{} is considered non-applicable", actionableGene);
                    return false;
                }
            }
        }

        return eventIsApplicable(actionableGene.gene(), actionableGene);
    }

    @VisibleForTesting
    static <T extends ActionableEvent> boolean eventIsApplicable(@NotNull String gene, @NotNull T event) {
        if (NON_APPLICABLE_GENES.contains(gene)) {
            LOGGER.debug("{} is considered non-applicable", event);
            return false;
        }

        return true;
    }
}
