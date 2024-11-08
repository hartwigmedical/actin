package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import org.assertj.core.api.Assertions
import org.junit.Test

class CurrentlyGetsTransporterMedicationTest {
    private val alwaysActiveFunction =
        CurrentlyGetsTransporterMedication(MedicationTestFactory.alwaysActive(), "BCRP", DrugInteraction.Type.SUBSTRATE)
    private val alwaysPlannedFunction =
        CurrentlyGetsTransporterMedication(MedicationTestFactory.alwaysPlanned(), "BCRP", DrugInteraction.Type.SUBSTRATE)

    @Test
    fun `Should pass with BCRP substrate medication`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withTransporterInteraction("BCRP", DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when no BCRP substrate medication`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withTransporterInteraction("BCRP", DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should warn when patient plans to use BCRP substrate medication`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withTransporterInteraction("BCRP", DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when patient plans to use medication which is not BCRP substrate medication`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withTransporterInteraction("BCRP", DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG)
            )
        )
    }


    @Test
    fun `Should fail when patient uses no medication`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList()))
        )
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val medicationNotProvided = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        val alwaysPlannedResult = alwaysPlannedFunction.evaluate(medicationNotProvided)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, alwaysPlannedResult)
        Assertions.assertThat(alwaysPlannedResult.recoverable).isTrue()
        val alwaysActiveResult = alwaysActiveFunction.evaluate(medicationNotProvided)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, alwaysActiveResult)
        Assertions.assertThat(alwaysActiveResult.recoverable).isTrue()
    }
}