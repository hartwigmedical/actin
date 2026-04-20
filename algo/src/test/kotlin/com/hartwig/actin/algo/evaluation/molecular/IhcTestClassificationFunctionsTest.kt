package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.IhcTestResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class IhcTestClassificationFunctionsTest {

    @ParameterizedTest
    @CsvSource(
        "0, NEGATIVE",
        "1, LOW",
        "2, BORDERLINE",
        "3, POSITIVE"
    )
    fun `classifyHer2Test should classify HER2 exact values into correct bucket`(value: Double, expected: String) {
        assertThat(classifyHer2(lower = value, upper = value)).isEqualTo(IhcTestResult.valueOf(expected))
    }

    @Test
    fun `classifyHer2Test should classify HER2 as NEGATIVE when only upper bound is known and within negative range`() {
        assertThat(classifyHer2(lower = null, upper = 0.0)).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @Test
    fun `classifyHer2Test should classify HER2 as POSITIVE when only lower bound is known and at or above positive threshold`() {
        assertThat(classifyHer2(lower = 3.0, upper = null)).isEqualTo(IhcTestResult.POSITIVE)
    }

    @Test
    fun `classifyHer2Test should classify HER2 range as BORDERLINE when both bounds within borderline range`() {
        assertThat(classifyHer2(lower = 1.0, upper = 2.0)).isEqualTo(IhcTestResult.BORDERLINE)
    }

    @Test
    fun `classifyHer2Test should give UNKNOWN for HER2 when both bounds are null`() {
        assertThat(classifyHer2(lower = null, upper = null)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyHer2Test should give UNKNOWN for HER2 when upper bound is null and lower is below positive threshold`() {
        assertThat(classifyHer2(lower = 2.0, upper = null)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyHer2Test should give UNKNOWN for HER2 when lower bound is null and upper exceeds negative range`() {
        assertThat(classifyHer2(lower = null, upper = 1.0)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyHer2Test should give UNKNOWN for HER2 when unit is wrong`() {
        assertThat(classifyHer2(lower = 2.0, upper = 2.0, unit = "%")).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyHer2Test should give UNKNOWN for HER2 when unit is missing`() {
        assertThat(classifyHer2(lower = 2.0, upper = 2.0, unit = null)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyHer2Test should classify score of zero as NEGATIVE regardless of unit`() {
        assertThat(classifyHer2(lower = 0.0, upper = 0.0, unit = null)).isEqualTo(IhcTestResult.NEGATIVE)
        assertThat(classifyHer2(lower = 0.0, upper = 0.0, unit = "%")).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @Test
    fun `classifyHer2Test should give UNKNOWN for HER2 when indeterminate flag is set`() {
        assertThat(classifyHer2(lower = 1.0, upper = 1.0, indeterminate = true)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @ParameterizedTest
    @CsvSource(
        "2, 3", // could be borderline or positive
        "1, 3", // could be low, borderline or positive
        "0, 3") // could be negative, low, borderline, or positive
    fun `classifyHer2Test should give UNKNOWN for HER2 crossing range boundary`(lower: Double, upper: Double) {
        assertThat(classifyHer2(lower = lower, upper = upper)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @ParameterizedTest
    @CsvSource("negative", "absent", "loss")
    fun `classifyHer2Test should classify HER2 as NEGATIVE based on scoreText`(text: String) {
        assertThat(classifyHer2(scoreText = text)).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @Test
    fun `classifyHer2Test should classify HER2 as LOW based on scoreText`() {
        assertThat(classifyHer2(scoreText = "low")).isEqualTo(IhcTestResult.LOW)
    }

    @ParameterizedTest
    @CsvSource("positive", "strong positive", "present", "detected", "overexpression")
    fun `classifyHer2Test should classify HER2 as POSITIVE based on scoreText`(text: String) {
        assertThat(classifyHer2(scoreText = text)).isEqualTo(IhcTestResult.POSITIVE)
    }

    @Test
    fun `classifyHer2Test should prioritize negative text over positive numeric value`() {
        assertThat(classifyHer2(lower = 3.0, upper = 3.0, scoreText = "negative")).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @Test
    fun `classifyHer2Test should prioritize numeric negative over positive text`() {
        assertThat(classifyHer2(lower = 0.0, upper = 0.0, scoreText = "positive")).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @ParameterizedTest
    @CsvSource(
        "0, NEGATIVE",
        "0.5, NEGATIVE",
        "5, LOW",
        "11, POSITIVE",
        "100, POSITIVE"
    )
    fun `classifyPrOrErTest should classify exact values into correct bucket`(value: Double, expected: String) {
        assertThat(classifyPrOrEr(lower = value, upper = value)).isEqualTo(IhcTestResult.valueOf(expected))
    }

    @ParameterizedTest
    @CsvSource("0", "0.8")
    fun `classifyPrOrErTest should classify as NEGATIVE when only upper bound is known and within negative range`(upper: Double) {
        assertThat(classifyPrOrEr(lower = null, upper = upper)).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @Test
    fun `classifyPrOrErTest should classify as LOW when both bounds within low range`() {
        assertThat(classifyPrOrEr(lower = 1.0, upper = 10.0)).isEqualTo(IhcTestResult.LOW)
    }

    @Test
    fun `classifyPrOrErTest should classify as POSITIVE when only lower bound is known and at or above positive threshold`() {
        assertThat(classifyPrOrEr(lower = 10.0, upper = null)).isEqualTo(IhcTestResult.POSITIVE)
    }

    @Test
    fun `classifyPrOrErTest should give UNKNOWN when lower bound is null and upper exceeds negative range`() {
        assertThat(classifyPrOrEr(lower = null, upper = 10.0)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyPrOrErTest should give UNKNOWN when upper bound is null and lower is below positive threshold`() {
        assertThat(classifyPrOrEr(lower = 9.0, upper = null)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyPrOrErTest should give UNKNOWN when both bounds are null`() {
        assertThat(classifyPrOrEr(lower = null, upper = null)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 5", // could be negative or low
        "5, 15", // could be low or positive
        "0, 100") // could be negative, low, or positive
    fun `classifyPrOrErTest should give UNKNOWN for crossing range boundary`(lower: Double, upper: Double) {
        assertThat(classifyPrOrEr(lower = lower, upper = upper)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyPrOrErTest should give UNKNOWN when unit is wrong`() {
        assertThat(classifyPrOrEr(lower = 5.0, upper = 5.0, unit = "+")).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyPrOrErTest should give UNKNOWN when unit is missing`() {
        assertThat(classifyPrOrEr(lower = 5.0, upper = 5.0, unit = null)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyPrOrErTest should give UNKNOWN when indeterminate flag is set`() {
        assertThat(classifyPrOrEr(lower = 50.0, upper = 50.0, indeterminate = true)).isEqualTo(IhcTestResult.UNKNOWN)
    }

    @Test
    fun `classifyPrOrErTest should prioritize negative text over positive numeric value`() {
        assertThat(classifyPrOrEr(lower = 3.0, upper = 3.0, scoreText = "negative")).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @Test
    fun `classifyPrOrErTest should prioritize numeric negative over positive text`() {
        assertThat(classifyPrOrEr(lower = 0.0, upper = 0.0, scoreText = "positive")).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @ParameterizedTest
    @CsvSource("negative", "absent", "loss")
    fun `classifyPrOrErTest should classify PR or ER as NEGATIVE based on scoreText`(text: String) {
        assertThat(classifyPrOrEr(scoreText = text)).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @Test
    fun `classifyPrOrErTest should classify PR or ER as LOW based on scoreText`() {
        assertThat(classifyPrOrEr(scoreText = "low")).isEqualTo(IhcTestResult.LOW)
    }

    @ParameterizedTest
    @CsvSource("positive", "strong positive", "present", "detected", "overexpression")
    fun `classifyPrOrErTest should classify PR or ER as POSITIVE based on scoreText`(text: String) {
        assertThat(classifyPrOrEr(scoreText = text)).isEqualTo(IhcTestResult.POSITIVE)
    }

    @Test
    fun `classifyPrOrErTest should classify score of zero as NEGATIVE regardless of unit`() {
        assertThat(classifyPrOrEr(lower = 0.0, upper = 0.0, unit = null)).isEqualTo(IhcTestResult.NEGATIVE)
        assertThat(classifyPrOrEr(lower = 0.0, upper = 0.0, unit = "+")).isEqualTo(IhcTestResult.NEGATIVE)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, NEGATIVE",
        "1, 1, LOW",
        "1, 10, LOW",
        "10, 10, LOW",
        "11, 11, POSITIVE",
        "100, 100, POSITIVE",
    )
    fun `classifyPrOrErTest should classify positive boundary values`(lower: Double, upper: Double, expected: String) {
        assertThat(classifyPrOrEr(lower = lower, upper = upper)).isEqualTo(IhcTestResult.valueOf(expected))
    }

    private fun classifyHer2(
        lower: Double? = null,
        upper: Double? = null,
        unit: String? = "+",
        scoreText: String? = null,
        indeterminate: Boolean = false
    ) = classify(IhcTestClassificationFunctions::classifyHer2Test, lower, upper, unit, scoreText, indeterminate)

    private fun classifyPrOrEr(
        lower: Double? = null,
        upper: Double? = null,
        unit: String? = "%",
        scoreText: String? = null,
        indeterminate: Boolean = false
    ) = classify(IhcTestClassificationFunctions::classifyPrOrErTest, lower, upper, unit, scoreText, indeterminate)

    private fun classify(
        classifyFn: (IhcTest) -> IhcTestResult,
        lower: Double?,
        upper: Double?,
        unit: String?,
        scoreText: String?,
        indeterminate: Boolean
    ): IhcTestResult {
        return classifyFn(
            MolecularTestFactory.ihcTest(
                scoreLowerBound = lower,
                scoreUpperBound = upper,
                scoreValueUnit = unit,
                scoreText = scoreText,
                impliesIndeterminate = indeterminate
            )
        )
    }
}
