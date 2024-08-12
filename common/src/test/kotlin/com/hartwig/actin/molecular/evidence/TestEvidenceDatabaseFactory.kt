package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.known.TestKnownEventResolverFactory
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase

object TestEvidenceDatabaseFactory {

    fun createProperDatabase(): EvidenceDatabase {
        return EvidenceDatabase(TestKnownEventResolverFactory.createProper(), TestActionableEventMatcherFactory.createProper())
    }
}
