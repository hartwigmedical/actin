package com.hartwig.actin.report.interpretation

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.clinical.datamodel.IHC_TEST_TYPE
import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager

class PriorIHCTestInterpreter {
    private val logger = LogManager.getLogger(PriorIHCTestInterpreter::class.java)

    private val interpretationBuilder = PriorMolecularTestInterpretationBuilder()

    fun interpret(record: PatientRecord): List<PriorMolecularTestInterpretation> {
        record.priorIHCTests.filter { it.test == IHC_TEST_TYPE }.forEach(::interpret)
        return interpretationBuilder.build()
    }

    private fun interpret(test: PriorIHCTest) {
        val item = test.item ?: ""
        val type = test.test
        val scoreText = test.scoreText
        val scoreValue = test.scoreValue
        when {
            scoreText != null -> interpretationBuilder.addInterpretation(type, scoreText, item, 0)
            scoreValue != null -> interpretationBuilder.addInterpretation(type, item, formatValueBasedPriorTest(test), 1)
            else -> logger.error("IHC test is neither text-based nor value-based: {}", test)
        }
    }

    private fun formatValueBasedPriorTest(valueTest: PriorIHCTest): String {
        return valueTest.scoreValue?.let {
            return listOfNotNull(
                "Score", valueTest.measure, valueTest.scoreValuePrefix, Formats.twoDigitNumber(it) + valueTest.scoreValueUnit
            ).joinToString(" ")
        } ?: ""
    }
}