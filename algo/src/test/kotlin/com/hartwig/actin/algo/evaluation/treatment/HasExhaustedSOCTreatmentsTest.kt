package com.hartwig.actin.algo.evaluation.treatment

import com.google.common.collect.Lists
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import org.junit.Test

class HasExhaustedSOCTreatmentsTest {
    @Test
    fun canEvaluate() {
        val function = HasExhaustedSOCTreatments()
        // No treatments
        val treatments: MutableList<PriorTumorTreatment> = Lists.newArrayList()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // One random treatment
        treatments.add(TreatmentTestFactory.builder().build())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }
}