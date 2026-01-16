package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WhoFunctionsTest {

    private val now = LocalDate.now()

    @Test
    fun `Should evaluate at most ranges`() {
        assertThat(whoStatus(0, WhoStatusPrecision.EXACT).isAtMost(1)).isTrue
        assertThat(whoStatus(1, WhoStatusPrecision.EXACT).isAtMost(1)).isTrue
        assertThat(whoStatus(2, WhoStatusPrecision.EXACT).isAtMost(1)).isFalse

        assertThat(whoStatus(1, WhoStatusPrecision.AT_MOST).isAtMost(0)).isFalse
        assertThat(whoStatus(1, WhoStatusPrecision.AT_MOST).isAtMost(1)).isTrue
        assertThat(whoStatus(1, WhoStatusPrecision.AT_MOST).isAtMost(2)).isTrue
        assertThat(whoStatus(1, WhoStatusPrecision.AT_MOST).isAtMost(3)).isTrue
        assertThat(whoStatus(1, WhoStatusPrecision.AT_MOST).isAtMost(4)).isTrue
    }

    @Test
    fun `Should evaluate an exact WHO`() {
        assertThat(whoStatus(0, WhoStatusPrecision.EXACT).isExactly(1)).isFalse
        assertThat(whoStatus(1, WhoStatusPrecision.EXACT).isExactly(1)).isTrue
        assertThat(whoStatus(2, WhoStatusPrecision.EXACT).isExactly(1)).isFalse

        assertThat(whoStatus(1, WhoStatusPrecision.AT_MOST).isExactly(1)).isFalse
        assertThat(whoStatus(1, WhoStatusPrecision.AT_LEAST).isExactly(1)).isFalse
    }

    @Test
    fun `Should evaluate convertion to text`() {
        assertThat(whoStatus(1, WhoStatusPrecision.EXACT).asText()).isEqualTo("1")
        assertThat(whoStatus(1, WhoStatusPrecision.AT_LEAST).asText()).isEqualTo(">=1")
        assertThat(whoStatus(1, WhoStatusPrecision.AT_MOST).asText()).isEqualTo("<=1")
    }

    @Test
    fun `Should evaluate convertion to range`() {
        assertThat(whoStatus(1, WhoStatusPrecision.EXACT).asRange()).isEqualTo(1..1)
        assertThat(whoStatus(1, WhoStatusPrecision.AT_LEAST).asRange()).isEqualTo(1..5)
        assertThat(whoStatus(1, WhoStatusPrecision.AT_MOST).asRange()).isEqualTo(0..1)
    }

    private fun whoStatus(value: Int, precision: WhoStatusPrecision) =
        WhoStatus(now, value, precision)

}