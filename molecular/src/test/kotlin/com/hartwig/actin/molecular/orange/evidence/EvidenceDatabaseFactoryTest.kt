package com.hartwig.actin.molecular.orange.evidence

import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.ImmutableKnownEvents
import com.hartwig.serve.datamodel.KnownEvents
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceDatabaseFactoryTest {

    @Test
    fun `Should create from minimal inputs`() {
        val knownEvents: KnownEvents = ImmutableKnownEvents.builder().build()
        val actionableEvents: ActionableEvents = ImmutableActionableEvents.builder().build()
        val doidEntry = TestDoidEntryFactory.createMinimalTestDoidEntry()

        assertThat(EvidenceDatabaseFactory.create(knownEvents, actionableEvents, doidEntry, emptySet())).isNotNull()
    }
}