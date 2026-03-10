package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val WHO_2 = 2

class WhoStatusFunctionsTest {

    private val now = LocalDate.now()
    private val whoExact1 = whoStatus(1, WhoStatusPrecision.EXACT)
    private val whoExact2 = whoStatus(2, WhoStatusPrecision.EXACT)
    private val whoExact3 = whoStatus(3, WhoStatusPrecision.EXACT)
    private val whoAtLeast1 = whoStatus(1, WhoStatusPrecision.AT_LEAST)
    private val whoAtLeast3 = whoStatus(3, WhoStatusPrecision.AT_LEAST)
    private val whoAtMost1 = whoStatus(1, WhoStatusPrecision.AT_MOST)
    private val whoAtMost3 = whoStatus(3, WhoStatusPrecision.AT_MOST)

    @Test
    fun `Should convert WHO status to text`() {
        assertThat(whoExact1.asText()).isEqualTo("1")
        assertThat(whoAtLeast1.asText()).isEqualTo(">=1")
        assertThat(whoAtMost1.asText()).isEqualTo("<=1")
    }

    @Test
    fun `Should convert WHO status to range`() {
        assertThat(whoExact1.asRange()).isEqualTo(1..1)
        assertThat(whoAtLeast1.asRange()).isEqualTo(1..5)
        assertThat(whoAtMost1.asRange()).isEqualTo(0..1)
    }

    @Test
    fun `Should pass for WHO inputs that equal the requested value`() {
        assertThat(whoExact2.isEqualTo(WHO_2)).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should fail for WHO inputs that for certain do not equal the requested value`() {
        assertThat(whoExact1.isEqualTo(WHO_2)).isEqualTo(EvaluationResult.FAIL)
        assertThat(whoExact3.isEqualTo(WHO_2)).isEqualTo(EvaluationResult.FAIL)
        assertThat(whoAtLeast3.isEqualTo(WHO_2)).isEqualTo(EvaluationResult.FAIL)
        assertThat(whoAtMost1.isEqualTo(WHO_2)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should be undetermined for WHO inputs that may equal the requested value`() {
        assertThat(whoAtLeast1.isEqualTo(WHO_2)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(whoAtMost3.isEqualTo(WHO_2)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @Test
    fun `Should pass for WHO inputs that cannot exceed the requested value for isAtMost function`() {
        assertThat(whoExact1.isAtMost(WHO_2)).isEqualTo(EvaluationResult.PASS)
        assertThat(whoExact2.isAtMost(WHO_2)).isEqualTo(EvaluationResult.PASS)
        assertThat(whoAtMost1.isAtMost(WHO_2)).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should fail for WHO inputs that for certain exceed the requested value for isAtMost function`() {
        assertThat(whoExact3.isAtMost(WHO_2)).isEqualTo(EvaluationResult.FAIL)
        assertThat(whoAtLeast3.isAtMost(WHO_2)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should be undetermined for WHO inputs that may exceed the requested value for isAtMost function`() {
        assertThat(whoAtLeast1.isAtMost(WHO_2)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(whoAtMost3.isAtMost(WHO_2)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @Test
    fun `Should pass for WHO inputs that are above the requested value for isAtLeast function`() {
        assertThat(whoExact2.isAtLeast(WHO_2)).isEqualTo(EvaluationResult.PASS)
        assertThat(whoExact3.isAtLeast(WHO_2)).isEqualTo(EvaluationResult.PASS)
        assertThat(whoAtLeast3.isAtLeast(WHO_2)).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should fail for WHO inputs that are below the requested value for isAtLeast function`() {
        assertThat(whoExact1.isAtLeast(WHO_2)).isEqualTo(EvaluationResult.FAIL)
        assertThat(whoAtMost1.isAtLeast(WHO_2)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should be undetermined for WHO inputs that can be above or below the requested value for isAtLeast function`() {
        assertThat(whoAtLeast1.isAtLeast(WHO_2)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(whoAtMost3.isAtLeast(WHO_2)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    private fun whoStatus(value: Int, precision: WhoStatusPrecision) =
        WhoStatus(now, value, precision)
}