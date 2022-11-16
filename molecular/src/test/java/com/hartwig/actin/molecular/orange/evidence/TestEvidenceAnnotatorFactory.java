package com.hartwig.actin.molecular.orange.evidence;

import com.google.common.collect.Lists;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;

import org.jetbrains.annotations.NotNull;

public final class TestEvidenceAnnotatorFactory {

    private TestEvidenceAnnotatorFactory() {
    }

    @NotNull
    public static EvidenceAnnotator createWithNoEvidence() {
        return new EvidenceAnnotator(ImmutableKnownEvents.builder().build(),
                ImmutableActionableEvents.builder().build(),
                Lists.newArrayList());
    }
}
