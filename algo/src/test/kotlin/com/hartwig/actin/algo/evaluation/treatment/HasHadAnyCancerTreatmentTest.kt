package com.hartwig.actin.algo.evaluation.treatment

import com.google.common.collect.Lists
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import org.junit.Test

class HasHadAnyCancerTreatmentTest {
    @Test
    fun canEvaluate() {
        val function = HasHadAnyCancerTreatment()

        // No treatments
        val treatments: MutableList<PriorTumorTreatment> = Lists.newArrayList()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // One random treatment
        treatments.add(TreatmentTestFactory.builder().build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }
}