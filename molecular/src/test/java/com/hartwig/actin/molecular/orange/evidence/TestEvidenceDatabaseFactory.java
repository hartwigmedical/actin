package com.hartwig.actin.molecular.orange.evidence;

import com.hartwig.actin.molecular.orange.evidence.actionable.TestActionableEventResolverFactory;
import com.hartwig.actin.molecular.orange.evidence.known.TestKnownEventResolverFactory;

import org.jetbrains.annotations.NotNull;

public final class TestEvidenceDatabaseFactory {

    private TestEvidenceDatabaseFactory() {
    }

    @NotNull
    public static EvidenceDatabase createEmptyDatabase() {
        return new EvidenceDatabase(TestKnownEventResolverFactory.createEmpty(), TestActionableEventResolverFactory.createEmpty());
    }
}
