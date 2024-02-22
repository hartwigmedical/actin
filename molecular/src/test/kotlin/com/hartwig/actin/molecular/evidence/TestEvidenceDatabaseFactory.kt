package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.actionability.TestActionableEventMatcherFactory
import com.hartwig.actin.molecular.evidence.known.TestKnownEventResolverFactory

object TestEvidenceDatabaseFactory {

    fun createEmptyDatabase(): EvidenceDatabase {
        return EvidenceDatabase(TestKnownEventResolverFactory.createEmpty(), TestActionableEventMatcherFactory.createEmpty())
    }

    fun createProperDatabase(): EvidenceDatabase {
        return EvidenceDatabase(TestKnownEventResolverFactory.createProper(), TestActionableEventMatcherFactory.createProper())
    }
}
