package com.hartwig.actin.molecular.orange.evidence;

import com.hartwig.actin.molecular.orange.evidence.actionable.TestActionableEventMatcherFactory;
import com.hartwig.actin.molecular.orange.evidence.known.TestKnownEventResolverFactory;

import org.jetbrains.annotations.NotNull;

public final class TestEvidenceDatabaseFactory {

    private TestEvidenceDatabaseFactory() {
    }

    @NotNull
    public static EvidenceDatabase createEmptyDatabase() {
        return new EvidenceDatabase(TestKnownEventResolverFactory.createEmpty(), TestActionableEventMatcherFactory.createEmpty());
    }
}
