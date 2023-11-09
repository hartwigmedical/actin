package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Test
import java.time.LocalDate

class HasLimitedBodyWeightTest {
    @Test
    fun canEvaluate() {
        val referenceDate = LocalDate.of(2023, 11, 9)
        val function = HasLimitedBodyWeight(150.0)

        // No weights, cannot determine
        val weights: MutableList<BodyWeight> = mutableListOf()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))

        // Most recent too high
        weights.add(weight().date(referenceDate.minusDays(5)).value(155.0).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))

        // A later one does succeed
        weights.add(weight().date(referenceDate.minusDays(4)).value(149.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))

        // An even later one has wrong unit
        weights.add(weight().date(referenceDate.minusDays(3)).value(148.0).unit("pounds").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    companion object {
        private fun weight(): ImmutableBodyWeight.Builder {
            return VitalFunctionTestFactory.bodyWeight().unit(HasLimitedBodyWeight.EXPECTED_UNIT)
        }
    }
}