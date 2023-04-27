package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class HasPotentialUncontrolledTumorRelatedPainTest {
    @Test
    fun canEvaluateOnComplication() {
        val function = HasPotentialUncontrolledTumorRelatedPain { MedicationStatusInterpretation.ACTIVE }
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(null)))
        val wrong: Complication = ComplicationTestFactory.builder().addCategories("just a category").build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(wrong)))
        val match: Complication = ComplicationTestFactory.builder()
            .addCategories("this is category: " + HasPotentialUncontrolledTumorRelatedPain.SEVERE_PAIN_COMPLICATION)
            .build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)))
    }

    @Test
    fun canEvaluateOnMedication() {
        val function = HasPotentialUncontrolledTumorRelatedPain { MedicationStatusInterpretation.ACTIVE }
        val wrong: Medication = TestMedicationFactory.builder().name("just some medication").build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withMedication(wrong)))
        val match: Medication =
            TestMedicationFactory.builder().name(HasPotentialUncontrolledTumorRelatedPain.SEVERE_PAIN_MEDICATION).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withMedication(match)))
    }
}