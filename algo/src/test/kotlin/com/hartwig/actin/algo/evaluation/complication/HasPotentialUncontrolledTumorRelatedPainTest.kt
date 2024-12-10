package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import org.junit.Test

private val OPIOIDS_ATC_LEVEL = AtcLevel("N02A", "Opioids")
private val PAIN_MEDICATION =
    TestMedicationFactory.createMinimal().copy(atc = AtcTestFactory.atcClassification().copy(pharmacologicalSubGroup = OPIOIDS_ATC_LEVEL))

class HasPotentialUncontrolledTumorRelatedPainTest {

    val alwaysActiveFunction = HasPotentialUncontrolledTumorRelatedPain(MedicationTestFactory.alwaysActive(), setOf(OPIOIDS_ATC_LEVEL))
    val alwaysPlannedFunction = HasPotentialUncontrolledTumorRelatedPain(MedicationTestFactory.alwaysPlanned(), setOf(OPIOIDS_ATC_LEVEL))

    @Test
    fun `Should evaluate to undetermined if patient has complication indicating pain`() {
        val painComplication = ComplicationTestFactory.complication(categories = setOf("pain category"))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysActiveFunction.evaluate(ComplicationTestFactory.withComplication(painComplication))
        )
    }

    @Test
    fun `Should evaluate to undetermined if patient uses severe pain medication`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysActiveFunction.evaluate(ComplicationTestFactory.withMedication(PAIN_MEDICATION))
        )
    }

    @Test
    fun `Should evaluate to undetermined if patient has planned severe pain medication`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysPlannedFunction.evaluate(ComplicationTestFactory.withMedication(PAIN_MEDICATION))
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