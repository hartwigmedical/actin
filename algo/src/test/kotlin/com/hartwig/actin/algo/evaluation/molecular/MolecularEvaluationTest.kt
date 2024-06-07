package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val PASS_ORANGE = EvaluationFactory.pass("pass orange")
private val UNDETERMINED_ORANGE = EvaluationFactory.undetermined("undetermined orange")
private val FAIL_ORANGE = EvaluationFactory.undetermined("fail orange")
private val PASS_PANEL = EvaluationFactory.pass("pass panel")
private val DEFAULT_UNDETERMINED = EvaluationFactory.undetermined("")

class MolecularEvaluationTest {

    @Test
    fun `Should return null evaluation when empty list passed`() {
        val molecularEvaluation = MolecularEvaluation(defaultUndetermined = DEFAULT_UNDETERMINED)
        assertThat(molecularEvaluation.combined()).isNull()
    }

    @Test
    fun `Should prefer pass evaluation from molecular record`() {
        val molecularEvaluation =
            MolecularEvaluation(molecularRecordEvaluation = PASS_ORANGE, panelEvaluations = listOf(PASS_PANEL), DEFAULT_UNDETERMINED)
        assertThat(molecularEvaluation.combined()).isEqualTo(PASS_ORANGE)
    }

    @Test
    fun `Should prefer pass evaluation from panel over warn from molecular record`() {
        val molecularEvaluation =
            MolecularEvaluation(
                molecularRecordEvaluation = UNDETERMINED_ORANGE,
                panelEvaluations = listOf(PASS_PANEL),
                DEFAULT_UNDETERMINED
            )
        assertThat(molecularEvaluation.combined()).isEqualTo(PASS_PANEL)
    }

    @Test
    fun `Should combine panel events with the same evaluation result`() {
        val molecularEvaluation =
            MolecularEvaluation(
                molecularRecordEvaluation = FAIL_ORANGE,
                panelEvaluations = listOf(
                    PASS_PANEL,
                    PASS_PANEL.copy(passSpecificMessages = setOf("pass panel 2"), passGeneralMessages = setOf("pass panel 2"))
                ),
                DEFAULT_UNDETERMINED
            )
        assertThat(molecularEvaluation.combined().passSpecificMessages).containsExactly("pass panel", "pass panel 2")
        assertThat(molecularEvaluation.combined().passGeneralMessages).containsExactly("pass panel", "pass panel 2")
    }
}