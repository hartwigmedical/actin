package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val ORANGE_EVENT = "orange event"
private val PASS_ORANGE = EvaluationFactory.pass("pass orange", inclusionEvents = setOf(ORANGE_EVENT))
private val UNDETERMINED_ORANGE = EvaluationFactory.undetermined("undetermined orange")
private val FAIL_ORANGE = EvaluationFactory.undetermined("fail orange")
private const val PANEL_EVENT = "panel event"
private val PASS_PANEL = EvaluationFactory.pass("pass panel", inclusionEvents = setOf(PANEL_EVENT))
private val DEFAULT_UNDETERMINED = EvaluationFactory.undetermined("")

class MolecularEvaluationTest {

    @Test
    fun `Should only return WGS results when rule passes`() {
        val combined = MolecularEvaluation.combine(
            listOf(
                MolecularEvaluation(TestMolecularFactory.createMinimalTestMolecularRecord(), PASS_ORANGE),
                MolecularEvaluation(TestPanelRecordFactory.empty(), PASS_PANEL),
            ),
            DEFAULT_UNDETERMINED
        )
        assertThat(combined.result).isEqualTo(EvaluationResult.PASS)
        assertThat(combined.inclusionMolecularEvents).containsExactly(ORANGE_EVENT)
    }

}