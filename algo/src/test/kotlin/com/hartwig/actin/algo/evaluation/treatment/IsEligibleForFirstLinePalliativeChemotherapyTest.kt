package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import org.junit.Test

class IsEligibleForFirstLinePalliativeChemotherapyTest {

    private val function = IsEligibleForFirstLinePalliativeChemotherapy()

    @Test
    fun `Should fail when previous palliative chemotherapy`() {
        val treatment = TreatmentTestFactory.drugTreatment("drug therapy", TreatmentCategory.CHEMOTHERAPY)
        val patientRecord =
            TreatmentTestFactory.withTreatmentHistoryEntry(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.PALLIATIVE)
                )
            )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail when no metastatic cancer`() {
        //val patientRecord = TumorTestFactory.withLiverLesions(null)
        //EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when patient has metastatic cancer and previous palliative therapy`() {
        val treatment = TreatmentTestFactory.drugTreatment("drug therapy", TreatmentCategory.TARGETED_THERAPY)
        val patientRecord =
            TreatmentTestFactory.withTreatmentHistoryEntry(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.PALLIATIVE)
                )
            )

        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when patient has metastatic cancer and no previous palliative therapy`() {
        //EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }
}