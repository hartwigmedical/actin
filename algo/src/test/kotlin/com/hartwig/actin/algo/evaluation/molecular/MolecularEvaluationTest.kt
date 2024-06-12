package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val ORANGE_EVENT = "orange event"
private val PASS_ORANGE = EvaluationFactory.pass("pass orange", inclusionEvents = setOf(ORANGE_EVENT))
private val WARN_ORANGE = EvaluationFactory.pass("warn orange", inclusionEvents = setOf(ORANGE_EVENT))
private val UNDETERMINED_ORANGE = EvaluationFactory.undetermined("undetermined orange")
private val FAIL_ORANGE = EvaluationFactory.undetermined("fail orange")
private const val PANEL_EVENT = "panel event"
private val PASS_PANEL = EvaluationFactory.pass("pass panel", inclusionEvents = setOf(PANEL_EVENT))
private val DEFAULT_UNDETERMINED = EvaluationFactory.undetermined("")

class MolecularEvaluationTest {

    @Test
    fun `Should only return WGS results when rule passes`() {
        combineAndAssert(
            PASS_ORANGE, MolecularEvaluation(TestPanelRecordFactory.empty(), PASS_PANEL),
            MolecularEvaluation(TestMolecularFactory.createMinimalTestMolecularRecord(), PASS_ORANGE)
        )
    }

    @Test
    fun `Should return combined panel results when panel passes and WGS fails`() {
        val panelEvent2 = "panel event 2"
        combineAndAssert(
            EvaluationFactory.pass("pass combined", inclusionEvents = setOf(PANEL_EVENT, panelEvent2)),
            MolecularEvaluation(TestMolecularFactory.createMinimalTestMolecularRecord(), FAIL_ORANGE),
            MolecularEvaluation(TestPanelRecordFactory.empty().copy(type = ExperimentType.ARCHER), PASS_PANEL),
            MolecularEvaluation(
                TestPanelRecordFactory.empty().copy(type = ExperimentType.GENERIC_PANEL),
                PASS_PANEL.copy(inclusionMolecularEvents = setOf(panelEvent2))
            )
        )
    }

    @Test
    fun `Should prefer pass over warn, warn over fail, and fail over undetermined`() {
        combineAndAssert(
            PASS_ORANGE, MolecularEvaluation(TestMolecularFactory.createMinimalTestMolecularRecord(), WARN_ORANGE),
            MolecularEvaluation(TestMolecularFactory.createMinimalTestMolecularRecord(), PASS_ORANGE)
        )
        combineAndAssert(
            WARN_ORANGE, MolecularEvaluation(TestMolecularFactory.createMinimalTestMolecularRecord(), FAIL_ORANGE),
            MolecularEvaluation(TestMolecularFactory.createMinimalTestMolecularRecord(), WARN_ORANGE)
        )
        combineAndAssert(
            FAIL_ORANGE, MolecularEvaluation(TestMolecularFactory.createMinimalTestMolecularRecord(), UNDETERMINED_ORANGE),
            MolecularEvaluation(TestMolecularFactory.createMinimalTestMolecularRecord(), FAIL_ORANGE)
        )
    }

    private fun combineAndAssert(expectedEvaluation: Evaluation, vararg evaluations: MolecularEvaluation) {
        val combined = MolecularEvaluation.combine(
            listOf(*evaluations),
            DEFAULT_UNDETERMINED
        )
        assertThat(combined.result).isEqualTo(expectedEvaluation.result)
        assertThat(combined.inclusionMolecularEvents).isEqualTo(expectedEvaluation.inclusionMolecularEvents)
    }
}