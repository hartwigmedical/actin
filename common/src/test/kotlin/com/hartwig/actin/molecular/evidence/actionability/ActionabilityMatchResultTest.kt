package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActionabilityMatchResultTest {

    private val actionable1 = mockk<Actionable>()
    private val actionable2 = mockk<Actionable>()

    @Test
    fun `Should combine MatchResults to Failure if any are Failure`() {
        val result1 = ActionabilityMatchResult.Success()
        val result2 = ActionabilityMatchResult.Failure

        val combinedResult = ActionabilityMatchResult.combine(listOf(result1, result2))

        assertThat(combinedResult).isEqualTo(ActionabilityMatchResult.Failure)
    }

    @Test
    fun `Should combine MatchResults to Success if all are Success`() {
        val result1 = ActionabilityMatchResult.Success()
        val result2 = ActionabilityMatchResult.Success()

        val combinedResult = ActionabilityMatchResult.combine(listOf(result1, result2))

        assertThat(combinedResult).isEqualTo(ActionabilityMatchResult.Success())
    }

    @Test
    fun `Should combine MatchResults to Success if empty`() {
        val combinedResult = ActionabilityMatchResult.combine(emptyList())
        assertThat(combinedResult).isEqualTo(ActionabilityMatchResult.Success())
    }

    @Test
    fun `Should aggregate actionables from all Success MatchResults`() {
        val actionabilityMatchResult1 = ActionabilityMatchResult.Success(listOf(actionable1))
        val actionabilityMatchResult2 = ActionabilityMatchResult.Success(listOf(actionable2))

        val combinedResult = ActionabilityMatchResult.combine(listOf(actionabilityMatchResult1, actionabilityMatchResult2))

        assertThat(combinedResult).isEqualTo(ActionabilityMatchResult.Success(listOf(actionable1, actionable2)))
    }
}