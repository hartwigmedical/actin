package com.hartwig.actin.molecular.orange.evidence;

import com.hartwig.actin.molecular.orange.evidence.actionability.TestActionableEventMatcherFactory;
import com.hartwig.actin.molecular.orange.evidence.known.TestKnownEventResolverFactory;

import org.jetbrains.annotations.NotNull;

public final class TestEvidenceDatabaseFactory {

    private TestEvidenceDatabaseFactory() {
    }

    @NotNull
    public static EvidenceDatabase createEmptyDatabase() {
        return new EvidenceDatabase(TestKnownEventResolverFactory.createEmpty(), TestActionableEventMatcherFactory.createEmpty());
    }

    @NotNull
    public static EvidenceDatabase createProperDatabase() {
        return new EvidenceDatabase(TestKnownEventResolverFactory.createProper(), TestActionableEventMatcherFactory.createProper());
    }
}
