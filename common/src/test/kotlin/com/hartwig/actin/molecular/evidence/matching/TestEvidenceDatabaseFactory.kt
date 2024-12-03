package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.evidence.actionability.TestActionableEventMatcherFactory
import com.hartwig.actin.molecular.evidence.known.TestKnownEventResolverFactory

object TestEvidenceDatabaseFactory {

    fun createProperDatabase(): EvidenceDatabase {
        return EvidenceDatabase(TestKnownEventResolverFactory.createProper(), TestActionableEventMatcherFactory.createProper())
    }
}
