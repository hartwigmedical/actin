package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasKnownSymptomaticCnsMetastasesTest {

    private val function = HasKnownSymptomaticCnsMetastases()

    @Test
    fun `Should return undetermined when unknown if (symptomatic) CNS or brain metastases present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = null,
                    hasSymptomaticBrainLesions = null,
                    hasCnsLesions = null,
                    hasSymptomaticCnsLesions = null
                )
            ),
            "Undetermined if symptomatic CNS metastases present (data missing)"
        )
    }

    @Test
    fun `Should return undetermined when CNS metastases present but unknown if symptomatic`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = false,
                    hasSymptomaticBrainLesions = null,
                    hasCnsLesions = true,
                    hasSymptomaticCnsLesions = null
                )
            ),
            "CNS metastases present but unknown if symptomatic (data missing)"
        )
    }

    @Test
    fun `Should return undetermined when brain metastases present but unknown if symptomatic`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = true,
                    hasSymptomaticBrainLesions = null,
                    hasCnsLesions = false,
                    hasSymptomaticCnsLesions = null
                )
            ),
            "CNS metastases present but unknown if symptomatic (data missing)"
        )
    }

    @Test
    fun `Should return undetermined when brain metastases are suspected but unknown if symptomatic`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = false,
                    hasSymptomaticBrainLesions = null,
                    hasCnsLesions = null,
                    hasSymptomaticCnsLesions = null,
                    hasSuspectedBrainLesions = true
                )
            ),
            "Suspected CNS metastases present but unknown if symptomatic (data missing)"
        )
    }

    @Test
    fun `Should return undetermined when CNS metastases are suspected but unknown if symptomatic`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = null,
                    hasSymptomaticBrainLesions = null,
                    hasCnsLesions = false,
                    hasSymptomaticCnsLesions = null,
                    hasSuspectedCnsLesions = true
                )
            ),
            "Suspected CNS metastases present but unknown if symptomatic (data missing)"
        )
    }

    @Test
    fun `Should fail when there are no CNS or brain metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = false,
                    hasSymptomaticBrainLesions = null,
                    hasCnsLesions = false,
                    hasSymptomaticCnsLesions = null
                )
            ),
            "No known symptomatic CNS metastases present"
        )
    }

    @Test
    fun `Should fail when CNS metastases are present but not symptomatic`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = true,
                    hasSymptomaticBrainLesions = null,
                    hasCnsLesions = true,
                    hasSymptomaticCnsLesions = false
                )
            ),
            "No known symptomatic CNS metastases present"
        )
    }

    @Test
    fun `Should pass when CNS metastases are present and symptomatic`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = false,
                    hasSymptomaticBrainLesions = null,
                    hasCnsLesions = true,
                    hasSymptomaticCnsLesions = true
                )
            ),
            "Has symptomatic CNS metastases"
        )
    }

    @Test
    fun `Should pass when brain metastases are present and symptomatic`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = true,
                    hasSymptomaticBrainLesions = true,
                    hasCnsLesions = false,
                    hasSymptomaticCnsLesions = false
                )
            ),
            "Has symptomatic CNS (Brain) metastases"
        )
    }

    @Test
    fun `Should pass when brain metastases are present and symptomatic and CNS metastases are present but not symptomatic`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withActiveAndSymptomaticBrainAndCnsLesionStatus(
                    hasBrainLesions = true,
                    hasSymptomaticBrainLesions = true,
                    hasCnsLesions = true,
                    hasSymptomaticCnsLesions = false
                )
            ),
            "Has symptomatic CNS (Brain) metastases"
        )
    }
}
