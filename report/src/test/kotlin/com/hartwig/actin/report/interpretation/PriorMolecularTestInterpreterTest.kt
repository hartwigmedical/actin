package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpreter.interpret
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorMolecularTestInterpreterTest {
    @Test
    fun shouldCorrectlyInterpretPriorMolecularTests() {
        val textBased1 = create("text 1", null, "test 1", "item 1")
        val textBased2 = create("text 1", null, "test 1", "item 2")
        val textBased3 = create("text 1", null, "test 2", "item 3")
        val valueBased1 = create(null, 1.0, "test 1", "item 4")
        val valueBased2 = create(null, 2.0, "test 2", "item 5")
        val invalid = create(null, null, "invalid", "invalid")

        val tests = listOf(textBased1, textBased2, textBased3, valueBased1, valueBased2, invalid)
        val interpretation = interpret(tests)

        assertThat(interpretation.textBasedPriorTests.keys).hasSize(2)
        assertThat(interpretation.textBasedPriorTests.values.flatten()).hasSize(3)
        assertThat(interpretation.valueBasedPriorTests).hasSize(2)
    }

    companion object {

        private fun create(scoreText: String?, scoreValue: Double?, test: String, item: String): PriorMolecularTest {
            return ImmutablePriorMolecularTest.builder()
                .test(test)
                .item(item)
                .scoreText(scoreText)
                .scoreValue(scoreValue)
                .impliesPotentialIndeterminateStatus(false)
                .build()
        }
    }
}