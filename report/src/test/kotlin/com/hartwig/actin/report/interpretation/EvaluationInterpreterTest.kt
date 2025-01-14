package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.trial.CriterionReference
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val FIRST_CRITERION = CriterionReference(id = "1. first", text = "test 1")
private val SECOND_CRITERION = CriterionReference(id = "2. second", text = "test 2")
private val THIRD_CRITERION = CriterionReference(id = "3. third", text = "test 3")
private val FOURTH_CRITERION = CriterionReference(id = "4. fourth", text = "test 4")

class EvaluationInterpreterTest {

    @Test
    fun `Should only generate FAIL when configured for FAIL only`() {
        val evaluations = mapOf(
            Pair(SECOND_CRITERION, createBaseEvaluation(result = EvaluationResult.WARN)),
            Pair(FIRST_CRITERION, createBaseEvaluation(result = EvaluationResult.FAIL)),
            Pair(THIRD_CRITERION, createBaseEvaluation(result = EvaluationResult.UNDETERMINED)),
            Pair(FOURTH_CRITERION, createBaseEvaluation(result = EvaluationResult.PASS))
        )
        val interpretation = EvaluationInterpreter.interpretForDetailedTrialMatching(evaluations, interpretFailOnly = true)

        assertThat(interpretation).isEqualTo(
            listOf(
                EvaluationInterpretation(
                    rule = FIRST_CRITERION.id,
                    reference = FIRST_CRITERION.text,
                    entriesPerResult = mapOf(
                        Pair(EvaluationResult.FAIL, EvaluationEntry("FAIL", setOf("fail 1", "fail 2"))),
                    )
                )
            )
        )
    }

    @Test
    fun `Should generate all when configured for all evaluations`() {
        val evaluations = mapOf(
            Pair(FOURTH_CRITERION, createBaseEvaluation(result = EvaluationResult.PASS)),
            Pair(SECOND_CRITERION, createBaseEvaluation(result = EvaluationResult.WARN)),
            Pair(FIRST_CRITERION, createBaseEvaluation(result = EvaluationResult.FAIL)),
            Pair(THIRD_CRITERION, createBaseEvaluation(result = EvaluationResult.UNDETERMINED))
        )
        val interpretation = EvaluationInterpreter.interpretForDetailedTrialMatching(evaluations, interpretFailOnly = false)

        assertThat(interpretation).isEqualTo(
            listOf(
                EvaluationInterpretation(
                    rule = FIRST_CRITERION.id,
                    reference = FIRST_CRITERION.text,
                    entriesPerResult = mapOf(
                        Pair(EvaluationResult.FAIL, EvaluationEntry("FAIL", setOf("fail 1", "fail 2"))),
                    )
                ),
                EvaluationInterpretation(
                    rule = SECOND_CRITERION.id,
                    reference = SECOND_CRITERION.text,
                    entriesPerResult = mapOf(
                        Pair(EvaluationResult.WARN, EvaluationEntry("WARN", setOf("warn 1", "warn 2"))),
                        Pair(EvaluationResult.UNDETERMINED, EvaluationEntry("UNDETERMINED", setOf("undetermined")))
                    )
                ),
                EvaluationInterpretation(
                    rule = THIRD_CRITERION.id,
                    reference = THIRD_CRITERION.text,
                    entriesPerResult = mapOf(
                        Pair(EvaluationResult.UNDETERMINED, EvaluationEntry("UNDETERMINED", setOf("undetermined")))
                    )
                ),
                EvaluationInterpretation(
                    rule = FOURTH_CRITERION.id,
                    reference = FOURTH_CRITERION.text,
                    entriesPerResult = mapOf(
                        Pair(EvaluationResult.PASS, EvaluationEntry("PASS", setOf("pass")))
                    )
                )
            )
        )
    }

    @Test
    fun `Should add spaces next to plus signs`() {
        val evaluations = mapOf(
            Pair(
                FIRST_CRITERION, createBaseEvaluation(result = EvaluationResult.WARN).copy(
                    failMessages = setOf(),
                    warnMessages = setOf("warn+me+now"),
                    undeterminedMessages = setOf(),
                    passMessages = setOf(),
                )
            ),
        )
        val interpretation = EvaluationInterpreter.interpretForDetailedTrialMatching(evaluations, interpretFailOnly = false)

        assertThat(interpretation).isEqualTo(
            listOf(
                EvaluationInterpretation(
                    rule = FIRST_CRITERION.id,
                    reference = FIRST_CRITERION.text,
                    entriesPerResult = mapOf(
                        Pair(EvaluationResult.WARN, EvaluationEntry("WARN", setOf("warn + me + now")))
                    )
                )
            )
        )
    }

    @Test
    fun `Should clarify recoverable in header and add WARN and UNDETERMINED`() {
        val evaluations = mapOf(
            Pair(FIRST_CRITERION, createBaseEvaluation(result = EvaluationResult.FAIL).copy(recoverable = true)),
        )
        val interpretation = EvaluationInterpreter.interpretForDetailedTrialMatching(evaluations, interpretFailOnly = false)

        assertThat(interpretation).isEqualTo(
            listOf(
                EvaluationInterpretation(
                    rule = FIRST_CRITERION.id,
                    reference = FIRST_CRITERION.text,
                    entriesPerResult = mapOf(
                        Pair(
                            EvaluationResult.FAIL,
                            EvaluationEntry("FAIL (potentially recoverable)", setOf("fail 1", "fail 2"))
                        ),
                        Pair(EvaluationResult.WARN, EvaluationEntry("WARN", setOf("warn 1", "warn 2"))),
                        Pair(EvaluationResult.UNDETERMINED, EvaluationEntry("UNDETERMINED", setOf("undetermined")))
                    )
                )
            )
        )
    }

    private fun createBaseEvaluation(result: EvaluationResult): Evaluation {
        return Evaluation(
            result = result,
            recoverable = false,
            passMessages = setOf("pass"),
            undeterminedMessages = setOf("undetermined"),
            warnMessages = setOf("warn 1", "warn 2"),
            failMessages = setOf("fail 1", "fail 2")
        )
    }
}