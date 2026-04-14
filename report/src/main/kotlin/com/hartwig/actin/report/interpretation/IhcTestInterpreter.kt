package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager
import java.time.LocalDate

const val IHC_TEST_TYPE = "IHC"

class IhcTestInterpreter {

    private val logger = LogManager.getLogger(IhcTestInterpreter::class.java)

    private val interpretationBuilder = IhcTestInterpretationBuilder()

    fun interpret(ihcTests: List<IhcTest>): List<IhcTestInterpretation> {
        val latestIhcTestsByItem = ihcTests.groupBy { it.item }.map { (_, tests) ->
            tests.maxByOrNull { it.measureDate ?: LocalDate.MIN } ?: tests.first()
        }
        latestIhcTestsByItem.forEach(::interpret)
        return interpretationBuilder.build()
    }

    private fun interpret(test: IhcTest) {
        val item = test.item
        val type = IHC_TEST_TYPE
        val date = test.measureDate
        val scoreText = test.scoreText
        val hasNumericScore = test.scoreLowerBound != null || test.scoreUpperBound != null
        when {
            scoreText != null && hasNumericScore -> {
                interpretationBuilder.addInterpretation(type, item, formatValueAndTextBasedIhcTest(test), date)
            }

            scoreText != null -> interpretationBuilder.addInterpretation(type, item, scoreText, date)
            hasNumericScore -> interpretationBuilder.addInterpretation(type, item, formatValueBasedIhcTest(test), date)
            else -> logger.error("IHC test is neither text-based nor value-based: {}", test)
        }
    }

    private fun formatValueAndTextBasedIhcTest(valueTest: IhcTest): String {
        return "${valueTest.scoreText}, ${formatValueBasedIhcTest(valueTest).replaceFirstChar { it.lowercase() }}"
    }

    private fun formatValueBasedIhcTest(valueTest: IhcTest): String {
        val formattedLowerValue = valueTest.scoreLowerBound?.let(Formats::twoDigitNumber)
        val formattedUpperValue = valueTest.scoreUpperBound?.let(Formats::twoDigitNumber)

        val formattedScore = when {
            formattedLowerValue != null && formattedUpperValue != null -> {
                if (formattedLowerValue == formattedUpperValue) formattedLowerValue else "$formattedLowerValue-$formattedUpperValue"
            }
            formattedLowerValue != null -> ">= $formattedLowerValue"
            formattedUpperValue != null -> "<= $formattedUpperValue"
            else -> return ""
        }
        return listOfNotNull("Score", valueTest.measure, formattedScore + valueTest.scoreValueUnit.orEmpty()).joinToString(" ")
    }
}
