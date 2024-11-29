package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionableEvents
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.KnownEvents
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceDatabaseFactoryTest {

    @Test
    fun `Should create from minimal inputs`() {
        val knownEvents: KnownEvents = ImmutableKnownEvents.builder().build()
        val actionableEvents = ActionableEvents()
        val doidEntry = TestDoidEntryFactory.createMinimalTestDoidEntry()

        assertThat(EvidenceDatabaseFactory.create(knownEvents, actionableEvents, doidEntry, emptySet())).isNotNull()
    }
}