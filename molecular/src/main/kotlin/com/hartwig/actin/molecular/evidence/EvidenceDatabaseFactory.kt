package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventMatcherFactory
import com.hartwig.actin.molecular.evidence.known.KnownEventResolverFactory
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.KnownEvents

object EvidenceDatabaseFactory {

    fun create(
        knownEvents: KnownEvents,
        actionableEvents: ActionableEvents,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>
    ): EvidenceDatabase {
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val factory = ActionableEventMatcherFactory(doidModel, tumorDoids)
        val actionableEventMatcher = factory.create(actionableEvents)
        val knownEventResolver = KnownEventResolverFactory.create(knownEvents)

        return EvidenceDatabase(knownEventResolver, actionableEventMatcher)
    }
}
