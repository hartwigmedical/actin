package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import org.junit.Test

private val PAIN_MEDICATION = AtcLevel("N02A", "Opioids")

class HasPotentialUncontrolledTumorRelatedPainTest {

    val alwaysActiveFunction = HasPotentialUncontrolledTumorRelatedPain(MedicationTestFactory.alwaysActive(), setOf(PAIN_MEDICATION))
    val alwaysPlannedFunction = HasPotentialUncontrolledTumorRelatedPain(MedicationTestFactory.alwaysPlanned(), setOf(PAIN_MEDICATION))

    @Test
    fun `Should pass if patient has complication indicating pain`() {
        val painComplication = ComplicationTestFactory.complication(categories = setOf("pain category"))
        assertEvaluation(EvaluationResult.PASS, alwaysActiveFunction.evaluate(ComplicationTestFactory.withComplication(painComplication)))
    }

    @Test
    fun `Should pass if patient uses severe pain medication`() {
        val painMedication = TestMedicationFactory.createMinimal()
            .copy(atc = AtcTestFactory.atcClassification().copy(pharmacologicalSubGroup = PAIN_MEDICATION))
        assertEvaluation(EvaluationResult.PASS, alwaysActiveFunction.evaluate(ComplicationTestFactory.withMedication(painMedication)))
    }

    @Test
    fun `Should warn if patient has planned severe pain medication`() {
        val plannedPainMedication = TestMedicationFactory.createMinimal()
            .copy(atc = AtcTestFactory.atcClassification().copy(pharmacologicalSubGroup = PAIN_MEDICATION))
        assertEvaluation(
            EvaluationResult.WARN,
            alwaysPlannedFunction.evaluate(ComplicationTestFactory.withMedication(plannedPainMedication))
        )
    }

    @Test
    fun `Should fail if patient has no complications`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(ComplicationTestFactory.withComplications(null)))
    }

    @Test
    fun `Should fail if patient has other complication than pain`() {
        val wrongComplication = ComplicationTestFactory.complication(categories = setOf("just a category"))
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(ComplicationTestFactory.withComplication(wrongComplication)))
    }

    @Test
    fun `Should fail if patient has no complications and uses no pain medication`() {
        val noPainMedication = TestMedicationFactory.createMinimal().copy(name = "just some medication")
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(ComplicationTestFactory.withMedication(noPainMedication)))
    }
}