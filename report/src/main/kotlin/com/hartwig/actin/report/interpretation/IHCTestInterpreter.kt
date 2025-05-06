package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager

const val IHC_TEST_TYPE = "IHC"

class IHCTestInterpreter {

    private val logger = LogManager.getLogger(IHCTestInterpreter::class.java)

    private val interpretationBuilder = IHCTestInterpretationBuilder()

    fun interpret(ihcTests: List<IHCTest>): List<IHCTestInterpretation> {
        ihcTests.forEach(::interpret)
        return interpretationBuilder.build()
    }

    private fun interpret(test: IHCTest) {
        val item = test.item ?: ""
        val type = IHC_TEST_TYPE
        val date = test.measureDate
        val scoreText = test.scoreText
        val scoreValue = test.scoreValue
        when {
            scoreText != null -> interpretationBuilder.addInterpretation(type, scoreText, item, date, 0)
            scoreValue != null -> interpretationBuilder.addInterpretation(type, item, formatValueBasedIHCTest(test), date, 1)
            else -> logger.error("IHC test is neither text-based nor value-based: {}", test)
        }
    }

    private fun formatValueBasedIHCTest(valueTest: IHCTest): String {
        return valueTest.scoreValue?.let {
            return listOfNotNull(
                "Score", valueTest.measure, valueTest.scoreValuePrefix, Formats.twoDigitNumber(it) + valueTest.scoreValueUnit.orEmpty()
            ).joinToString(" ")
        } ?: ""
    }
}