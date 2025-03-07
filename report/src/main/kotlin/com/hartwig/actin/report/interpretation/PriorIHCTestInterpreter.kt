package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager

class PriorIHCTestInterpreter {

    private val logger = LogManager.getLogger(PriorIHCTestInterpreter::class.java)

    private val interpretationBuilder = PriorMolecularTestInterpretationBuilder()

    fun interpret(record: PatientRecord): List<PriorMolecularTestInterpretation> {
        IhcTestFilter.mostRecentOrUnknownDateIhcTests(record.priorIHCTests).forEach(::interpret)
        return interpretationBuilder.build()
    }

    private fun interpret(test: PriorIHCTest) {
        val item = test.item ?: ""
        val type = test.test
        val date = test.measureDate
        val scoreText = test.scoreText
        val scoreValue = test.scoreValue
        when {
            scoreText != null -> interpretationBuilder.addInterpretation(type, scoreText, item, date, 0)
            scoreValue != null -> interpretationBuilder.addInterpretation(type, item, formatValueBasedPriorTest(test), date, 1)
            else -> logger.error("IHC test is neither text-based nor value-based: {}", test)
        }
    }

    private fun formatValueBasedPriorTest(valueTest: PriorIHCTest): String {
        return valueTest.scoreValue?.let {
            return listOfNotNull(
                "Score", valueTest.measure, valueTest.scoreValuePrefix, Formats.twoDigitNumber(it) + valueTest.scoreValueUnit.orEmpty()
            ).joinToString(" ")
        } ?: ""
    }
}