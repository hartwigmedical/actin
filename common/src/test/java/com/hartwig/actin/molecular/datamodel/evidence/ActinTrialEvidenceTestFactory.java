package com.hartwig.actin.molecular.datamodel.evidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class ActinTrialEvidenceTestFactory {

    private ActinTrialEvidenceTestFactory() {
    }

    @NotNull
    public static ImmutableActinTrialEvidence.Builder builder() {
        return ImmutableActinTrialEvidence.builder()
                .event(Strings.EMPTY)
                .trialAcronym(Strings.EMPTY)
                .isInclusionCriterion(true)
                .type(MolecularEventType.SIGNATURE);
    }
}
