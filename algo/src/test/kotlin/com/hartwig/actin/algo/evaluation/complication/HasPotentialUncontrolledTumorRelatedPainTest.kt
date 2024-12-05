package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import org.junit.Test

class HasPotentialUncontrolledTumorRelatedPainTest {

    @Test
    fun `Should evaluate on complication`() {
        val function = HasPotentialUncontrolledTumorRelatedPain(medicationStatusInterpreter)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(null)))
        val wrong: Complication = ComplicationTestFactory.complication(categories = setOf("just a category"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(wrong)))
        val match: Complication = ComplicationTestFactory.complication(
            categories = setOf("this is category: " + HasPotentialUncontrolledTumorRelatedPain.SEVERE_PAIN_COMPLICATION)
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)))
    }

    @Test
    fun `Should evaluate on medication`() {
        val function = HasPotentialUncontrolledTumorRelatedPain(medicationStatusInterpreter)
        val wrong: Medication = TestMedicationFactory.createMinimal().copy(name = "just some medication")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withMedication(wrong)))
        val match: Medication =
            TestMedicationFactory.createMinimal().copy(name = HasPotentialUncontrolledTumorRelatedPain.SEVERE_PAIN_MEDICATION)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withMedication(match)))
    }

    companion object {
        private val medicationStatusInterpreter = object : MedicationStatusInterpreter {
            override fun interpret(medication: Medication): MedicationStatusInterpretation {
                return MedicationStatusInterpretation.ACTIVE
            }
        }
    }
}