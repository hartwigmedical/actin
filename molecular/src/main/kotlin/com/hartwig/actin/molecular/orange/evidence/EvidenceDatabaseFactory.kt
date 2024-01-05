package com.hartwig.actin.molecular.orange.evidence

import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionableEventMatcherFactory
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapper
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapping
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolverFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.KnownEvents

object EvidenceDatabaseFactory {

    fun create(
        knownEvents: KnownEvents, actionableEvents: ActionableEvents,
        externalTrialMappings: List<ExternalTrialMapping>, doidEntry: DoidEntry, tumorDoids: Set<String>
    ): EvidenceDatabase {
        val externalTrialMapper = ExternalTrialMapper(externalTrialMappings)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val factory = ActionableEventMatcherFactory(externalTrialMapper, doidModel, tumorDoids)
        val actionableEventMatcher = factory.create(actionableEvents)
        val knownEventResolver = KnownEventResolverFactory.create(knownEvents)

        return EvidenceDatabase(knownEventResolver, actionableEventMatcher)
    }
}
