package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventMatcherFactory
import com.hartwig.actin.molecular.evidence.known.KnownEventResolverFactory
import com.hartwig.serve.datamodel.ServeRecord

object EvidenceDatabaseFactory {

    fun create(
        serveRecord: ServeRecord,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>
    ): EvidenceDatabase {
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val factory = ActionableEventMatcherFactory(doidModel, tumorDoids)
        val actionableEventMatcher = factory.create(serveRecord.evidences(), serveRecord.trials())
        val knownEventResolver = KnownEventResolverFactory.create(serveRecord.knownEvents())

        return EvidenceDatabase(knownEventResolver, actionableEventMatcher)
    }
}
