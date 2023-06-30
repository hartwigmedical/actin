package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseEntryFactory.createEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CTCDatabaseEntryInterpreterTest {
    private val entries = listOf(
        createEntry(PARENT_ID, null, "Open", 1),
        createEntry(2, PARENT_ID, "Open", 1),
        createEntry(3, PARENT_ID, "Open", 0),
        createEntry(4, PARENT_ID, "Gesloten", 0),
        createEntry(5, PARENT_ID, "Open", 1)
    )

    @Test
    fun noMatchIsConsideredInvalid() {
        assertThat(CTCDatabaseEntryInterpreter.hasValidCTCDatabaseMatches(emptyList())).isFalse
    }

    @Test
    fun nullMatchIsConsideredInvalid() {
        assertThat(CTCDatabaseEntryInterpreter.hasValidCTCDatabaseMatches(listOf(null))).isFalse
    }

    @Test
    fun entriesWithBothParentsAndChildAreConsideredInvalid() {
        assertThat(CTCDatabaseEntryInterpreter.hasValidCTCDatabaseMatches(entries)).isFalse
    }

    @Test
    fun singleEntryIsAlwaysConsideredValid() {
        for (entry in entries) {
            assertThat(CTCDatabaseEntryInterpreter.hasValidCTCDatabaseMatches(listOf(entry))).isTrue
        }
    }

    companion object {
        private const val PARENT_ID = 1
    }
}