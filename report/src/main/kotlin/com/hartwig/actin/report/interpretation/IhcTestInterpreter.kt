package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager

const val IHC_TEST_TYPE = "IHC"

class IhcTestInterpreter {

    private val logger = LogManager.getLogger(IhcTestInterpreter::class.java)

    private val interpretationBuilder = IhcTestInterpretationBuilder()

    fun interpret(ihcTests: List<IhcTest>): List<IhcTestInterpretation> {
        val latestIhcTestsByItem = ihcTests.groupBy { it.item }.map { (_, testsForItem) ->
            val testsForItemWithMeasureDate = testsForItem.filter { it.measureDate != null }
            testsForItemWithMeasureDate.maxByOrNull { it.measureDate!! } ?: testsForItem.first()
        }
        latestIhcTestsByItem.forEach(::interpret)
        return interpretationBuilder.build()
    }

    private fun interpret(test: IhcTest) {
        val item = test.item
        val type = IHC_TEST_TYPE
        val date = test.measureDate
        val scoreText = test.scoreText
        val scoreValue = test.scoreValue
        when {
            scoreText != null && scoreValue != null -> interpretationBuilder.addInterpretation(
                type,
                item,
                formatValueAndTextBasedIhcTest(test),
                date,
                1
            )

            scoreText != null -> interpretationBuilder.addInterpretation(type, scoreText, item, date, 0)
            scoreValue != null -> interpretationBuilder.addInterpretation(type, item, formatValueBasedIhcTest(test), date, 1)
            else -> logger.error("IHC test is neither text-based nor value-based: {}", test)
        }
    }

    private fun formatValueAndTextBasedIhcTest(valueTest: IhcTest): String {
        return "${valueTest.scoreText}, ${formatValueBasedIhcTest(valueTest).replaceFirstChar { it.lowercase() }}"
    }

    private fun formatValueBasedIhcTest(valueTest: IhcTest): String {
        return valueTest.scoreValue?.let {
            return listOfNotNull(
                "Score", valueTest.measure, valueTest.scoreValuePrefix, Formats.twoDigitNumber(it) + valueTest.scoreValueUnit.orEmpty()
            ).joinToString(" ")
        } ?: ""
    }
}