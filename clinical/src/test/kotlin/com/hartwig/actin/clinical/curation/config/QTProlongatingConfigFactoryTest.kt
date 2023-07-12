package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test


class QTProlongatingConfigFactoryTest {

    @Test
    fun shouldThrowIllegalStateWhenRiskIsUnknownValue() {
        assertThrows(IllegalStateException::class.java) {
            val victim = QTProlongatingConfigFactory()
            victim.create(mapOf("Name" to 0, "Risk" to 1), arrayOf("medicationName", "not_a_risk"))
        }
    }

    @Test
    fun shouldReturnCorrectEnumValueRegardlessOfCaseOrWhitespace() {
        val victim = QTProlongatingConfigFactory()
        val result = victim.create(mapOf("Name" to 0, "Risk" to 1), arrayOf("medicationName", "known "))
        assertThat(result).extracting { it.status }.isEqualTo(QTProlongatingRisk.KNOWN)
    }
}