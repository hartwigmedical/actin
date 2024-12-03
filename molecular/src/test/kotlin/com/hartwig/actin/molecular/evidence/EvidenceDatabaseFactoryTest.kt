package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceDatabaseFactoryTest {

    @Test
    fun `Should create from minimal inputs`() {
        val serveRecord = ImmutableServeRecord.builder()
            .knownEvents(ImmutableKnownEvents.builder().build())
            .evidences(emptyList())
            .trials(emptyList())
            .build()
        val doidEntry = TestDoidEntryFactory.createMinimalTestDoidEntry()

        assertThat(EvidenceDatabaseFactory.create(serveRecord, doidEntry, emptySet())).isNotNull()
    }
}