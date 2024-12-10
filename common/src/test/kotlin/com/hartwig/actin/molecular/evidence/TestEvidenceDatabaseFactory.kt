package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.actionability.TestClinicalEvidenceMatcherFactory
import com.hartwig.actin.molecular.evidence.known.TestKnownEventResolverFactory

object TestEvidenceDatabaseFactory {

    fun createProperDatabase(): EvidenceDatabase {
        return EvidenceDatabase(TestKnownEventResolverFactory.createProper(), TestClinicalEvidenceMatcherFactory.createProper())
    }
}
