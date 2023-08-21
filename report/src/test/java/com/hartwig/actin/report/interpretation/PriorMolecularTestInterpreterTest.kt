package com.hartwig.actin.report.interpretation

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpreter.interpret
import org.junit.Assert
import org.junit.Test

class PriorMolecularTestInterpreterTest {
    @Test
    fun canInterpretPriorMolecularTests() {
        val textBased1 = create("text 1", null, "test 1", "item 1")
        val textBased2 = create("text 1", null, "test 1", "item 2")
        val textBased3 = create("text 1", null, "test 2", "item 3")
        val valueBased1 = create(null, 1.0, "test 1", "item 4")
        val valueBased2 = create(null, 2.0, "test 2", "item 5")
        val invalid = create(null, null, "invalid", "invalid")
        val tests: List<PriorMolecularTest> = Lists.newArrayList(textBased1, textBased2, textBased3, valueBased1, valueBased2, invalid)
        val interpretation = interpret(tests)
        Assert.assertEquals(2, interpretation.textBasedPriorTests().keySet().size.toLong())
        Assert.assertEquals(3, interpretation.textBasedPriorTests().values().size.toLong())
        Assert.assertEquals(2, interpretation.valueBasedPriorTests().size.toLong())
    }

    companion object {
        private fun create(
            scoreText: String?, scoreValue: Double?, test: String,
            item: String
        ): PriorMolecularTest {
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