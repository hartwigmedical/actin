package com.hartwig.actin.molecular.orange.evidence

import com.google.common.collect.Lists
import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapping
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.ImmutableKnownEvents
import com.hartwig.serve.datamodel.KnownEvents
import org.junit.Assert.assertNotNull
import org.junit.Test

class EvidenceDatabaseFactoryTest {

    @Test
    fun canCreateFromMinimalInputs() {
        val knownEvents: KnownEvents = ImmutableKnownEvents.builder().build()
        val actionableEvents: ActionableEvents = ImmutableActionableEvents.builder().build()
        val externalTrialMappings: MutableList<ExternalTrialMapping> = Lists.newArrayList()
        val doidEntry = TestDoidEntryFactory.createMinimalTestDoidEntry()
        val tumorDoids: MutableSet<String> = mutableSetOf()

        assertNotNull(
            EvidenceDatabaseFactory.create(
                knownEvents,
                actionableEvents,
                externalTrialMappings,
                doidEntry,
                tumorDoids
            )
        )
    }
}