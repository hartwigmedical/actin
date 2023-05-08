package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Test
import java.time.LocalDate

class HasSufficientBodyWeightTest {
    @Test
    fun canEvaluate() {
        val referenceDate = LocalDate.of(2020, 8, 20)
        val function = HasSufficientBodyWeight(60.0)

        // No weights, cannot determine
        val weights: MutableList<BodyWeight> = mutableListOf()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))

        // Most recent too low
        weights.add(weight().date(referenceDate.minusDays(5)).value(50.0).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))

        // A later one does succeed
        weights.add(weight().date(referenceDate.minusDays(4)).value(70.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))

        // An even later one has wrong unit
        weights.add(weight().date(referenceDate.minusDays(3)).value(70.0).unit("pounds").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    companion object {
        private fun weight(): ImmutableBodyWeight.Builder {
            return VitalFunctionTestFactory.bodyWeight().unit(HasSufficientBodyWeight.EXPECTED_UNIT)
        }
    }
}