package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.IhcTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val DEFAULT_DATE = LocalDate.of(2025, 2, 10)
private val MORE_RECENT_DATE = LocalDate.of(2025, 2, 11)

class IhcTestInterpreterTest {

    private val interpreter = IhcTestInterpreter()

    @Test
    fun `Should interpret IHC test based on score text`() {
        val result = interpreter.interpret(ihcTests = listOf(ihcMolecularTest("HER2", "Positive")))
        assertThat(result).containsExactly(
            IhcTestInterpretation(
                "IHC", listOf(IhcTestResultInterpretation("Positive", "HER2", DEFAULT_DATE))
            )
        )
    }

    @Test
    fun `Should interpret IHC test based score value`() {
        val result = interpreter.interpret(ihcTests = listOf(ihcMolecularTest("HER2", scoreValue = 90.0, scoreValueUnit = "%")))
        assertThat(result).containsExactly(
            IhcTestInterpretation(
                "IHC", listOf(IhcTestResultInterpretation("HER2", "Score 90%", DEFAULT_DATE, 1))
            )
        )
    }

    @Test
    fun `Should interpret IHC test based on score text and score value`() {
        val result = interpreter.interpret(ihcTests = listOf(ihcMolecularTest("PD-L1", "Positive", 50.0, "%")))
        assertThat(result).containsExactly(
            IhcTestInterpretation(
                "IHC", listOf(IhcTestResultInterpretation("PD-L1", "Positive, score 50%", DEFAULT_DATE, sortPrecedence = 1))
            )
        )
    }

    @Test
    fun `Should correctly handle score based IHC test without unit`() {
        val result = interpreter.interpret(ihcTests = listOf(ihcMolecularTest("HER2", scoreValue = 90.0, scoreValueUnit = null)))
        assertThat(result).containsExactly(
            IhcTestInterpretation(
                "IHC", listOf(IhcTestResultInterpretation("HER2", "Score 90", DEFAULT_DATE, 1))
            )
        )
    }

    @Test
    fun `Should correctly handle null date`() {
        val result = interpreter.interpret(ihcTests = listOf(ihcMolecularTest("HER2", "Positive").copy(measureDate = null)))
        assertThat(result).containsExactly(
            IhcTestInterpretation(
                "IHC", listOf(IhcTestResultInterpretation("Positive", "HER2", null))
            )
        )
    }

    @Test
    fun `Should only interpret item with most recent date`() {
        val result = interpreter.interpret(
            ihcTests = listOf(
                ihcMolecularTest("HER2", "Positive"),
                ihcMolecularTest("HER2", "Negative").copy(measureDate = MORE_RECENT_DATE)
            )
        )
        assertThat(result).containsExactly(
            IhcTestInterpretation(
                "IHC", listOf(IhcTestResultInterpretation("Negative", "HER2", MORE_RECENT_DATE))
            )
        )
    }

    @Test
    fun `Should select item with date if there is one with and one without date`() {
        val result = interpreter.interpret(
            ihcTests = listOf(
                ihcMolecularTest("HER2", "Positive").copy(measureDate = null),
                ihcMolecularTest("HER2", "Negative")
            )
        )
        assertThat(result).containsExactly(
            IhcTestInterpretation(
                "IHC", listOf(IhcTestResultInterpretation("Negative", "HER2", DEFAULT_DATE))
            )
        )
    }

    @Test
    fun `Should select first item if only items without date`() {
        val result = interpreter.interpret(
            ihcTests = listOf(
                ihcMolecularTest("HER2", "Positive").copy(measureDate = null),
                ihcMolecularTest("HER2", "Negative").copy(measureDate = null)
            )
        )
        assertThat(result).containsExactly(
            IhcTestInterpretation(
                "IHC", listOf(IhcTestResultInterpretation("Positive", "HER2", null))
            )
        )
    }

    private fun ihcMolecularTest(protein: String, scoreText: String? = null, scoreValue: Double? = null, scoreValueUnit: String? = null) =
        IhcTest(
            item = protein,
            measureDate = DEFAULT_DATE,
            scoreText = scoreText,
            scoreValue = scoreValue,
            scoreValueUnit = scoreValueUnit,
            impliesPotentialIndeterminateStatus = false
        )
}