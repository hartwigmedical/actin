package com.hartwig.actin.molecular.orange.evidence

import com.hartwig.actin.molecular.orange.evidence.actionability.TestActionableEventMatcherFactory
import com.hartwig.actin.molecular.orange.evidence.known.TestKnownEventResolverFactory

object TestEvidenceDatabaseFactory {
    fun createEmptyDatabase(): EvidenceDatabase {
        return EvidenceDatabase(TestKnownEventResolverFactory.createEmpty(), TestActionableEventMatcherFactory.createEmpty())
    }

    fun createProperDatabase(): EvidenceDatabase {
        return EvidenceDatabase(TestKnownEventResolverFactory.createProper(), TestActionableEventMatcherFactory.createProper())
    }
}
