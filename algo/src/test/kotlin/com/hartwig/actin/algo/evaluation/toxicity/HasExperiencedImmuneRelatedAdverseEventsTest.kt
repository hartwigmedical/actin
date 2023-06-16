package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasExperiencedImmuneRelatedAdverseEventsTest {
    @Test
    fun canEvaluate() {
        val function = HasExperiencedImmuneRelatedAdverseEvents()

        // No prior treatments
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withPriorTumorTreatments(treatments)))

        // Treatment with mismatch category
        treatments.add(ToxicityTestFactory.treatment().addCategories(TreatmentCategory.RADIOTHERAPY).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withPriorTumorTreatments(treatments)))

        // Treatment with matching category
        treatments.add(ToxicityTestFactory.treatment().addCategories(TreatmentCategory.IMMUNOTHERAPY).build())
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ToxicityTestFactory.withPriorTumorTreatments(treatments)))
    }
}