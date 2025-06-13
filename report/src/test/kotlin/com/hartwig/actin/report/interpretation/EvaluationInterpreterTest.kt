package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val FIRST_CRITERION = "1. first"
private const val SECOND_CRITERION = "2. second"
private const val THIRD_CRITERION = "3. third"
private const val FOURTH_CRITERION = "4. fourth"

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
                    reference = FIRST_CRITERION,
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
                    reference = FIRST_CRITERION,
                    entriesPerResult = mapOf(
                        Pair(EvaluationResult.FAIL, EvaluationEntry("FAIL", setOf("fail 1", "fail 2"))),
                    )
                ),
                EvaluationInterpretation(
                    reference = SECOND_CRITERION,
                    entriesPerResult = mapOf(
                        Pair(EvaluationResult.WARN, EvaluationEntry("WARN", setOf("warn 1", "warn 2"))),
                        Pair(EvaluationResult.UNDETERMINED, EvaluationEntry("UNDETERMINED", setOf("undetermined")))
                    )
                ),
                EvaluationInterpretation(
                    reference = THIRD_CRITERION,
                    entriesPerResult = mapOf(
                        Pair(EvaluationResult.UNDETERMINED, EvaluationEntry("UNDETERMINED", setOf("undetermined")))
                    )
                ),
                EvaluationInterpretation(
                    reference = FOURTH_CRITERION,
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
                    warnMessages = setOf(StaticMessage("warn+me+now")),
                    undeterminedMessages = setOf(),
                    passMessages = setOf(),
                )
            ),
        )
        val interpretation = EvaluationInterpreter.interpretForDetailedTrialMatching(evaluations, interpretFailOnly = false)

        assertThat(interpretation).isEqualTo(
            listOf(
                EvaluationInterpretation(
                    reference = FIRST_CRITERION,
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
                    reference = FIRST_CRITERION,
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
            passMessages = setOf(StaticMessage("pass")),
            undeterminedMessages = setOf(StaticMessage("undetermined")),
            warnMessages = setOf(StaticMessage("warn 1"), StaticMessage("warn 2")),
            failMessages = setOf(StaticMessage("fail 1"), StaticMessage("fail 2"))
        )
    }
}